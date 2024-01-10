package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.exception.InternalServerException
import com.marufh.photo.exception.UnauthorizedException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.security.jwt.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import util.EncodeUtil
import util.FileUtil
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import ws.schild.jave.encode.VideoAttributes
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class FileService(
    val thumbService: ThumbService,
    val tokenProvider: TokenProvider,
    val resourceLoader: ResourceLoader,
    val fileMetaRepository: FileMetaRepository,
    val filePathProperties: FilePathProperties) {

    companion object {
        private val fileDatePattern = DateTimeFormatter.ofPattern("yyyy/MMMM/d")
        private var messageDigest: MessageDigest? = null

        init {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("cannot initialize SHA-512 hash function", e)
            }
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)

    fun uploadFile(multipartFile: MultipartFile) {
        val fileName: String = multipartFile.getOriginalFilename()!!
        log.info("Uploading file: {}", fileName)

        try {
            // 1. Validate file name and extension
            val name = validateFileNameAndExt(fileName!!)
            if (name == null) {
                log.error("File name is not valid")
                return
            }

            // 2. Save file on tmp location
            val file = saveOnTmpDirectory(multipartFile)

            // 3. Get file created date
            val dateDirectory: String = FileUtil.createdDate(file)
            val fileDestinationLocation: String = filePathProperties.media + dateDirectory + "/" + fileName
            val thumbDestinationLocation: String = filePathProperties.thumb + dateDirectory + "/" + fileName

            // 4. Check the file is existed or not
            val exist = checkFileExist(fileDestinationLocation)
            if (exist) {
                log.info("File {} is already exist", filePathProperties.media + dateDirectory + "/" + fileName)
                log.info("Deleting {} from /tmp ", file.absolutePath)
                Files.deleteIfExists(Path.of(file.absolutePath))
                return
            }

            // 5. Create directory if not exist
            Files.createDirectories(Path.of(filePathProperties.media + dateDirectory))

            // 6. Move file on final destination
            val destinationPath = moveOnFinalDestination(file, fileDestinationLocation)

            // 7. create thumb
            thumbService.create(destinationPath.toFile(), thumbDestinationLocation)

            // 8. Save file on db
            saveFile(destinationPath.toFile(), dateDirectory)
            log.info("File {} uploaded successfully", fileName)
        } catch (e: IOException) {
            log.error("Error while uploading file: {}", fileName, e)
        }
    }

    fun reUploadFile(multipartFile: MultipartFile, id: String) {
        val fileMeta: FileMeta = fileMetaRepository.findById(id)
            .orElseThrow { RuntimeException("File not found") }
        val oldFilePath: Path = Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src))
        val oldThumbPath: Path = Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.thumb))
        try {
            // Delete old file and thumb
            log.info("Deleting old file: {}", oldFilePath)
            log.info("Deleting old thumb: {}", oldThumbPath)
            Files.deleteIfExists(oldFilePath)
            Files.deleteIfExists(oldThumbPath)

            // Upload new file and thumb
            val fileName: String? = multipartFile.getOriginalFilename()
            val dateDirectory: String = fileMeta.base?.replace("/" + fileMeta.name, "") ?: ""
            val fileDestinationLocation: String = filePathProperties.base + EncodeUtil.decode(fileMeta.src)
            val thumbDestinationLocation: String = filePathProperties.base + EncodeUtil.decode(fileMeta.thumb)
            Files.copy(multipartFile.getInputStream(), Path.of(fileDestinationLocation))
            thumbService.create(File(fileDestinationLocation), thumbDestinationLocation)

            // Update file meta
            val src: String = Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.media))
                .toString() + "/" + dateDirectory + "/" + fileName
            val thumb: String = Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.thumb))
                .toString() + "/" + dateDirectory + "/" + fileName

            // Update file meta
            fileMeta.name = fileName!!
            fileMeta.base = "$dateDirectory/$fileName"
            fileMeta.src = EncodeUtil.encode(src)
            fileMeta.thumb = EncodeUtil.encode(prepareThumb(thumb, fileMeta.type))
            fileMeta.size = multipartFile.getSize()
            fileMeta.hash = getHash(File(fileDestinationLocation))
            fileMetaRepository.save(fileMeta)
        } catch (e: IOException) {
            throw InternalServerException(e.message!!)
        }
    }

    fun getFileAsResource(encodedPath: String?, token: String?): ResponseEntity<Resource> {
        if (!tokenProvider.validateToken(token)) {
            throw UnauthorizedException("Invalid Token")
        }
        log.info("Getting encodedPath: {}", encodedPath)
        val decodePhotoPath: String = EncodeUtil.decode(encodedPath)
        val path = Path.of(filePathProperties.base, decodePhotoPath)
        log.info("Getting resource: {}", path)
        val ext: String = FileUtil.getExtension(decodePhotoPath)
            ?: return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .contentType(MediaType.IMAGE_PNG)
                .body<Resource>(resourceLoader!!.getResource("classpath:image-default-thumb.png"))
        var resource: Resource? = null
        try {
            resource = UrlResource(path.toUri())
            if (!resource.exists()) {
                log.error("Resource not exist: {}", path)
                val fileType: FileType = FileUtil.fileTypeMap.get(ext.lowercase(Locale.getDefault()))!!
                if (fileType.equals(FileType.IMAGE)) {
                    resource = resourceLoader!!.getResource("classpath:image-default-thumb.png")
                } else if (fileType.equals(FileType.VIDEO)) {
                    resource = resourceLoader!!.getResource("classpath:video-default-thumb.jpg")
                }
            }
        } catch (e: IOException) {
            log.error("Loading image failed, {}", e.message)
        }
        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .contentType(getMedia(ext))
            .body<Resource>(resource)
    }

    fun moveFiles() {
        val start = Instant.now()
        //moveFileRecursively(new File(filePathProperties.getTmp()));
        val end = Instant.now()
        log.info("File move time: {}", Duration.between(start, end).toString())

        // After work1: Clean empty directory
        cleanEmptyDirectory(File(filePathProperties.tmp))

        // After work2: Clean empty directory
        //thumbService.create();

        // After work3: Clean empty directory
        //fileVisitService.visitFile();
    }

    fun fixCreateDateByName() {
        try {
            var pageRequest: Pageable = PageRequest.of(0, 500)
            var onePage: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)
            var count = 0
            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next()
                for (fileMeta in onePage.getContent()) {
                    if (FileUtil.DATE_PATTERN.matcher(fileMeta.name).find()) {
                        var name: String = fileMeta?.name!!
                        log.info("Name: {}", name)

                        // Remove prefix
                        if (name.startsWith("VID_")) {
                            name = name.replace("VID_", "")
                        }

                        // Remove prefix
                        if (name.startsWith("IMG")) {
                            name = name.replace("IMG_", "")
                        }

                        // Get year, month and day
                        val dateStr = name.substring(0, 4) + "-" + name.substring(4, 6) + "-" + name.substring(6, 8)
                        var nameDate: LocalDate
                        nameDate = try {
                            LocalDate.parse(dateStr)
                        } catch (e: DateTimeParseException) {
                            log.warn("Invalid date: {}", dateStr)
                            continue
                        }
                        if (fileMeta.createdAt.isEqual(nameDate)) {
                            log.warn("Created date: {} and name date are equal: {}", fileMeta.createdAt, dateStr)
                            continue
                        }
                        log.info("Date on file name: {}", dateStr)
                        log.info("Created date: {}", fileMeta.createdAt)
                        val pathInsideTarget = nameDate.year.toString() + "/" + nameDate.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                        ) + "/" + nameDate.dayOfMonth
                        val newSrcDir: String = filePathProperties.media + pathInsideTarget
                        if (!Files.exists(Path.of(newSrcDir))) {
                            log.info("Creating path: {}", newSrcDir)
                            Files.createDirectories(Path.of(newSrcDir))
                        }
                        val newThmDir: String = filePathProperties.thumb + pathInsideTarget
                        if (!Files.exists(Path.of(newThmDir))) {
                            log.info("Creating path: {}", newThmDir)
                            Files.createDirectories(Path.of(newThmDir))
                        }

                        // For source
                        val finalSrcPath = newSrcDir + "/" + fileMeta.name
                        if (!Files.exists(Path.of(finalSrcPath))) {
                            val sourcePath: Path = Path.of(filePathProperties.media + fileMeta.base)
                            val destinationPath = Path.of(finalSrcPath)
                            if (Files.exists(sourcePath)) {
                                Files.move(sourcePath, destinationPath)
                            }
                            log.info("{} moved to  {}", sourcePath, destinationPath)
                        }

                        // For thumb
                        val finalThumbPath = newThmDir + "/" + fileMeta.name
                        if (!Files.exists(Path.of(finalThumbPath))) {
                            val sourcePath: Path = Path.of(filePathProperties.thumb + fileMeta.base)
                            val destinationPath = Path.of(finalThumbPath)
                            if (Files.exists(sourcePath)) {
                                Files.move(sourcePath, destinationPath)
                            }
                            log.info("{} moved to  {}", sourcePath, destinationPath)
                        }
                        val basePath = pathInsideTarget + "/" + fileMeta.name
                        val src: String =
                            Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.media))
                                .toString()
                        val thumb: String =
                            Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.thumb))
                                .toString()
                        fileMeta.name = name
                        fileMeta.base = basePath
                        fileMeta.src = EncodeUtil.encode("$src/$basePath")
                        fileMeta.thumb = EncodeUtil.encode(
                                prepareThumb(
                                    thumb,
                                    Path.of(basePath),
                                    Path.of(fileMeta.name)
                                )
                            )

                        fileMeta.createdAt = nameDate
                        log.info("{}", fileMeta)
                        fileMetaRepository.save(fileMeta)
                        count++
                        println("----------------------------------------")
                    }
                }
                onePage = fileMetaRepository.findAll(pageRequest)
            }
            log.info("Total file changed: $count")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateHash() {
        log.info("Generating hash is started ")
        var pageRequest: Pageable = PageRequest.of(0, 500)
        var onePage: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)
        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next()
            for (fileMeta in onePage.content) {
                if (fileMeta?.hash != null) {
                    continue
                }
                log.info("Generating hash for: {}", fileMeta?.name)
                try {
                    val file: File = File(filePathProperties.media + fileMeta?.base)
                    val fi = FileInputStream(file)
                    val fileData = ByteArray(file.length().toInt())
                    fi.read(fileData)
                    fi.close()
                    val hash = BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
                    fileMeta?.hash = hash
                    fileMetaRepository.save(fileMeta)
                } catch (e: Exception) {
                    log.warn("Error generating hash: {}", e.message)
                }
            }
            onePage = fileMetaRepository.findAll(pageRequest)
        }
        log.info("Generating hash is finished")
    }

    fun generateThumb() {
        log.info("Generating thumb is started ")
        var pageRequest: Pageable = PageRequest.of(0, 500)
        var onePage: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)
        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next()
            val fileMetas: MutableList<FileMeta> = ArrayList<FileMeta>()
            for (fileMeta in onePage.getContent()) {
                if (fileMeta.thumb != null) {
                    continue
                }
                log.info("Generating thumb for: {}", fileMeta.name)
                try {
                    val file: File = File(filePathProperties.media + fileMeta.base)
                    val thumbFileStr: String = file.absolutePath.replace(filePathProperties.media, filePathProperties.thumb)
                    val thumbFile: File = thumbService.create(file, thumbFileStr)!!
                    val thumbPath: String = Path.of(filePathProperties.base).relativize(Path.of(thumbFile.absolutePath)).toString()
                    fileMeta.thumb = EncodeUtil.encode(thumbPath)
                    fileMetas.add(fileMeta)
                } catch (e: Exception) {
                    log.warn("Error generating thumb: {}", e.message)
                }
            }
            fileMetaRepository.saveAll(fileMetas)
            onePage = fileMetaRepository.findAll(pageRequest)
        }
        log.info("Generating thumb is finished")
    }

    private fun checkFileExist(fileDestinationLocation: String): Boolean {
        return Files.exists(Path.of(fileDestinationLocation))
    }

    fun saveFile(file: File, dateDirectory: String) {
        val name = file.name
        val thumb: String = Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.thumb))
            .toString() + "/" + dateDirectory + "/" + name
        val src: String = Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.media))
            .toString() + "/" + dateDirectory + "/" + name
        val fileType: FileType? = FileUtil.getExtension(name)?.let { FileUtil.fileTypeMap.get(it.lowercase()) }
        fileMetaRepository.save(
            FileMeta(
                name = name,
                type = fileType!!,
                caption = "",
                base = "$dateDirectory/$name",
                src = EncodeUtil.encode(src),
                thumb = EncodeUtil.encode(prepareThumb(thumb, fileType)),
                createdAt = LocalDate.parse(dateDirectory, fileDatePattern),
                size = file.length(),
                hash = getHash(file)
            )
        )
    }

    fun convertToMp4(id: String) {
        log.info("Converting to mp4: {}", id)
        val fileMeta: FileMeta = fileMetaRepository.findById(id)
            .orElseThrow { RuntimeException("File not found") }
        if (!FileUtil.getExtension(fileMeta.name).equals("mov", ignoreCase = true)) {
            log.warn("File is not mov: {}", fileMeta.name)
            return
        }
        log.info("Convert started")
        val src: String = filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src)
        val target: String =
            filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src).replace(".mov", ".mp4")
        val success = convert2mp4(src, target)
        if (success) {
            // Delete old file
            try {
                Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src)))
                fileMeta.src = EncodeUtil.encode(EncodeUtil.decode(fileMeta.src).replace(".mov", ".mp4"))
                fileMeta.base = fileMeta.base.replace(".mov", ".mp4")
                fileMeta.name = fileMeta.name.replace(".mov", ".mp4")
                fileMeta.caption = fileMeta.caption?.replace(".mov", ".mp4")
                fileMeta.thumb = EncodeUtil.encode(EncodeUtil.decode(fileMeta.thumb).replace(".mov", ".mp4"))
                fileMetaRepository.save(fileMeta)
                thumbService.create(File(target), filePathProperties.thumb + fileMeta.base)
                log.info("Convert finished")
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun convert2mp4(src: String, trg: String): Boolean {
        val source = File(src)
        val target = File(trg)
        val audio = AudioAttributes()
        audio.setCodec("aac")
        val video = VideoAttributes()
        video.setCodec("h264")
        val attrs = EncodingAttributes()
        attrs.setOutputFormat("mp4")
        attrs.setAudioAttributes(audio)
        attrs.setVideoAttributes(video)
        return try {
            val encoder = Encoder()
            encoder.encode(MultimediaObject(source), target, attrs)
            true
        } catch (e: Exception) {
            false
        }
    }

    @Throws(IOException::class)
    private fun saveOnTmpDirectory(multipartFile: MultipartFile): File {
        log.info("Saving file on tmp directory: {}", multipartFile.getOriginalFilename())
        val tmpPath: Path = Path.of(filePathProperties.tmp)
            .resolve(Objects.requireNonNull<String>(multipartFile.getOriginalFilename()))

        // Create parent directory if not exist
        if (!Files.exists(tmpPath.parent)) {
            log.info("Creating file parent directory: {}", tmpPath.parent)
            Files.createDirectories(tmpPath.parent)
        }

        // Copy file to tmp location
        Files.copy(multipartFile.getInputStream(), tmpPath)
        return tmpPath.toFile()
    }

    private fun validateFileNameAndExt(fileName: String): String? {
        log.info("Validating file name: {}", fileName)
        val name: String = FileUtil.getName(fileName)
        if (FileUtil.invalidName(name)) {
            log.error("Ignored file processing due to bad name: {}", name)
            return null
        }
        val extension: String? = FileUtil.getExtension(fileName)
        if (FileUtil.invalidExtension(FileUtil.allowExtension, extension)) {
            log.error("Ignored file processing due to extension: {}", extension)
            return null
        }
        return "$name.$extension"
    }

    @Throws(IOException::class)
    private fun moveOnFinalDestination(file: File, fileDestinationLocation: String): Path {
        var fileDestinationPath = Path.of(fileDestinationLocation)
        if (!Files.exists(fileDestinationPath)) {
            Files.move(Path.of(file.getCanonicalPath()), fileDestinationPath)
            log.info("{} moved to  {}", file.getCanonicalPath(), fileDestinationLocation)
        } else {
            val index = fileDestinationLocation.lastIndexOf("/")
            val part1 = fileDestinationLocation.substring(0, index + 1)
            val part2: String =
                FileUtil.getName(file.name) + "_" + UUID.randomUUID() + "." + FileUtil.getExtension(file.name) //UUID.randomUUID()+ "_" + fileDestinationLocation.substring(index + 1);
            fileDestinationPath = Path.of(part1 + part2)
            Files.move(Path.of(file.getCanonicalPath()), fileDestinationPath)
            log.info("{} moved to  {}", file.getCanonicalPath(), part1 + part2)
        }
        return fileDestinationPath
    }

    private fun cleanEmptyDirectory(file: File) {
        log.info("Start cleaning empty directory")
        try {
            Files.walk(file.toPath())
                .sorted(Comparator.reverseOrder())
                .map { obj: Path -> obj.toFile() }
                .filter { obj: File -> obj.isDirectory() }
                .forEach { obj: File -> obj.delete() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun prepareThumb(thumb: String, type: FileType): String {
        var thumb = thumb
        if (type.equals(FileType.VIDEO)) {
            thumb = "$thumb.jpeg"
        }
        return thumb
    }

    private fun prepareThumb(thumb: String, path: Path, name: Path): String {
        var thumbFile = "$thumb/$path"
        if (FileUtil.fileTypeMap.get(FileUtil.getExtension(name.toString())?.lowercase(Locale.getDefault())) === FileType.VIDEO) {
            thumbFile = "$thumbFile.jpeg"
        }
        return thumbFile
    }

    private fun getHash(file: File): String {
        val hash: String
        hash = try {
            val fi = FileInputStream(file)
            val fileData = ByteArray(file.length().toInt())
            fi.read(fileData)
            fi.close()
            BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return hash
    }

    private fun getMedia(ext: String): MediaType {
        val mime: String? = FileUtil.mimeTypeMapping.get(ext.lowercase(Locale.getDefault()))
        return if (mime != null) {
            MediaType.valueOf(mime)
        } else MediaType.APPLICATION_OCTET_STREAM
    }
}

package util

import java.util.*

class EncodeUtil private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        fun encode(strClearText: String): String {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(strClearText.toByteArray())
        }

        fun decode(strEncrypted: String?): String {
            return String(Base64.getUrlDecoder().decode(strEncrypted))
        }
    }
}

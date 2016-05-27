package org.akoshterek.backgammon.util

/**
  * @author oleksii.koshterek
  *         On: 27.05.16
  */
object Base64 {
    val aszBase64: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    def base64(ch: Byte): Byte = {
        if (ch >= 'A' && ch <= 'Z') return (ch - 'A').toByte
        if (ch >= 'a' && ch <= 'z') return ((ch - 'a') + 26).toByte
        if (ch >= '0' && ch <= '9') return ((ch - '0') + 52).toByte
        if (ch == '+') return 62.toByte
        if (ch == '/') return 63.toByte
        255.toByte
    }
}

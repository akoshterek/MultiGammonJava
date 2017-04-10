package org.akoshterek.backgammon.util

/**
  * @author oleksii.koshterek
  *         On: 27.05.16
  */
object Base64 {
    val aszBase64: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    def base64(ch: Byte): Byte = {
        if (ch >= 'A' && ch <= 'Z') (ch - 'A').toByte
        else if (ch >= 'a' && ch <= 'z') ((ch - 'a') + 26).toByte
        else if (ch >= '0' && ch <= '9') ((ch - '0') + 52).toByte
        else if (ch == '+') 62.toByte
        else if (ch == '/') 63.toByte
        else 255.toByte
    }
}

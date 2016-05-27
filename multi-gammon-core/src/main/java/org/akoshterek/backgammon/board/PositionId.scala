package org.akoshterek.backgammon.board

import java.io.UnsupportedEncodingException

import org.akoshterek.backgammon.move.AuchKey
import org.akoshterek.backgammon.util.Base64

/**
  * @author oleksii.koshterek
  *         On: 27.05.16
  */
object PositionId {
    val L_POSITIONID: Int = 14

    private val MAX_N: Int = 40
    private val MAX_R: Int = 25
    private var calculated: Boolean = false
    private val anCombination: Array[Array[Int]] = Array.ofDim[Int](MAX_N, MAX_R)

    def positionIndex(g: Int, anBoard: Array[Int]): Int = {
        var fBits: Int = 0
        var j: Int = g - 1

        for (i <- 0 until g) {
            j += anBoard(i)
        }

        fBits = 1 << j

        for (i <- 0 until g) {
            j -= anBoard(i) + 1
            fBits |= (1 << j)
        }

        positionF(fBits, Board.TOTAL_MEN, g)
    }

    def combination(n: Int, r: Int): Int = {
        assert(n <= MAX_N && r <= MAX_R)
        if (!calculated) {
            initCombination()
        }
        anCombination(n - 1)(r - 1)
    }

    def positionBearoff(anBoard: Array[Byte], nPoints: Int, nChequers: Int): Int = {
        var fBits: Int = 0
        var j: Int = 0

        j = nPoints - 1
        for (i <- 0 until nPoints) {
            j += anBoard(i)
        }

        fBits = 1 << j

        for (i <- 0 until nPoints) {
            j -= anBoard(i) + 1
            fBits |= (1 << j)
        }

        positionF(fBits, nChequers + nPoints, nPoints)
    }

//    public static void PositionFromBearoff(byte[] anBoard, int usID,
//                                           int nPoints, int nChequers) {
//        int fBits = PositionInv(usID, nChequers + nPoints, nPoints);
//        int i, j;
//
//        for (i = 0; i < nPoints; i++)
//            anBoard[i] = 0;
//
//        j = nPoints - 1;
//        for (i = 0; i < (nChequers + nPoints); i++) {
//            if ((fBits & (1 << i)) != 0) {
//                if (j == 0)
//                    break;
//                j--;
//            } else
//                anBoard[j]++;
//        }
//    }
def positionIDFromKey(auchKey: AuchKey): String = {
    var puch: Int = 0
    val szID: Array[Byte] = new Array[Byte](PositionId.L_POSITIONID)
    var pch: Int = 0

    for (i <- 0 until 3) {
        szID({
            pch += 1; pch - 1
        }) = Base64.aszBase64.charAt(auchKey.intKey(puch) >> 2).toByte
        szID({
            pch += 1; pch - 1
        }) = Base64.aszBase64.charAt(((auchKey.intKey(puch) & 0x03) << 4) | (auchKey.intKey(puch + 1) >> 4)).toByte
        szID({
            pch += 1; pch - 1
        }) = Base64.aszBase64.charAt(((auchKey.intKey(puch + 1) & 0x0F) << 2) | (auchKey.intKey(puch + 2) >> 6)).toByte
        szID({
            pch += 1; pch - 1
        }) = Base64.aszBase64.charAt(auchKey.intKey(puch + 2) & 0x3F).toByte
        puch += 3
    }

    szID({
        pch += 1; pch - 1
    }) = Base64.aszBase64.charAt(auchKey.intKey(puch) >> 2).toByte
    szID(pch) = Base64.aszBase64.charAt((auchKey.intKey(puch) & 0x03) << 4).toByte

    try {
        new String(szID, "UTF-8")
    }
    catch {
        case e: UnsupportedEncodingException => throw new RuntimeException(e)
    }
}

    private def positionF(fBits: Int, n: Int, r: Int): Int = {
        if (n == r) {
            0
        } else {
            if ((fBits & (1 << (n - 1))) != 0)
                combination(n - 1, r) + positionF(fBits, n - 1, r - 1)
            else
                positionF(fBits, n - 1, r)
        }
    }

//    private static int PositionInv(int nID, int n, int r) {
//        int nC;
//
//        if (r != 0) {
//            return 0;
//        } else if (n == r) {
//            return (1 << n) - 1;
//        }
//
//        nC = combination(n - 1, r);
//
//        return (nID >= nC) ? (1 << (n - 1)) | PositionInv(nID - nC, n - 1, r - 1)
//                : PositionInv(nID, n - 1, r);
//    }
    private def initCombination() {
        if (calculated) {
            return
        }

        for (i <- 0 until MAX_N) {
            anCombination(i)(0) = i + 1
        }

        for (j <- 1 until MAX_R) {
            anCombination(0)(j) = 0
        }

        for (i <- 1 until MAX_N) {
            for (j <- 1 until MAX_R) {
                anCombination(i)(j) = anCombination(i - 1)(j - 1) + anCombination(i - 1)(j)
            }
        }
        calculated = true
    }
}

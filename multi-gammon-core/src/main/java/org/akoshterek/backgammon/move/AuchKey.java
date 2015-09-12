package org.akoshterek.backgammon.move;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Alex
 *         date 19.07.2015.
 */
public class AuchKey {
    public final byte[] key = new byte[10];

    public AuchKey() {}

    public AuchKey(byte[] key) {
        System.arraycopy(key, 0, this.key, 0, this.key.length);
    }

    public int intKey(int index) {
        return key[index] < 0 ? 256 + key[index] : key[index];
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;

        AuchKey that = (AuchKey)o;
        return Arrays.equals(key, that.key);
    }

    public String toString() {
        try {
            return new String(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.akoshterek.backgammon.move;

import java.util.Arrays;

public class AuchKey {
    public final byte[] key = new byte[10];

    public AuchKey() {}

    public int intKey(int index) {
        return key[index] < 0 ? 256 + key[index] : key[index];
    }

    public static AuchKey fromNnPosition(String positionId) {
        if(positionId == null || positionId.length() != 20) {
            throw new IllegalArgumentException("Illegal position string " + positionId);
        }

        AuchKey key = new AuchKey();
        for (int i = 0; i < 10; i++) {
            char i2_0 = positionId.charAt(2 * i);
            char i2_1 = positionId.charAt(2 * i + 1);
            if (i2_0 >= 'A' && i2_0 <= 'P' && i2_1 >= 'A' && i2_1 <= 'P') {
                key.key[i] = (byte)(((i2_0 - 'A') << 4) + (i2_1 - 'A'));
            }
            else {
                throw new IllegalArgumentException("Illegal position string " + positionId);
            }
        }

        return key;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

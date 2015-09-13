package org.akoshterek.backgammon.bearoff;

import com.google.common.io.LittleEndianDataInputStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Alex
 *         date 31.08.2015.
 */
public class BearoffContext {
    private boolean isTwoSided; /* type of bearoff database */
    private int nPoints;    /* number of points covered by database */
    private int nChequers;  /* number of chequers for one-sided database */
    private String szFilename; /* filename */
    /* one sided dbs */
    private boolean fCompressed; /* is database compressed? */
    private boolean fGammon;     /* gammon probs included */

    private boolean fND;         /* normal distibution instead of exact dist? */
    /* two sided dbs */
    private boolean fCubeful;    /* cubeful equities included */
    private byte[] data;        /* pointer to data in memory */

    public static BearoffContext bearoffInit (String szFilename) {
        BearoffContext pbc = new BearoffContext();
        byte[] sz = new byte[40];

        pbc.szFilename = szFilename;

        try (LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(BearoffContext.class.getResourceAsStream(szFilename))) {
	        // read header
            inputStream.readFully(sz);
            // detect bearoff program
            String header = new String(sz, 0, 5, "UTF-8");
            if(!"gnubg".equals(header)) {
                throw new IllegalArgumentException("Unknown bearoff database");
            }

        	// one sided or two sided?
            String type = new String(sz, 6, 2, "UTF-8");
            if("TS".equals(type)) {
                pbc.isTwoSided = true;
            } else if("OS".equals(type)) {
                pbc.isTwoSided = false;
            } else {
                throw new IllegalArgumentException("Illegal bearoff type " + type);
            }

		    // number of points
            String pointsStr = new String(sz, 9, 2, "UTF-8");
            pbc.nPoints = Integer.valueOf(pointsStr);
            if (pbc.nPoints < 1 || pbc.nPoints >= 24) {
                throw new IllegalArgumentException("illegal number of points " + pbc.nPoints);
            }

		    // number of chequers
            String nChequersStr = new String(sz, 12, 2, "UTF-8");
            pbc.nChequers = Integer.valueOf(nChequersStr);
            if (pbc.nChequers < 1 || pbc.nChequers > 15) {
                throw new IllegalArgumentException("illegal number of chequers " + pbc.nChequers);
            }

            if (pbc.isTwoSided) {
                // options for two-sided dbs
                pbc.fCubeful = Integer.valueOf(new String(sz, 15, 1, "UTF-8")) != 0;
            } else {
		        // options for one-sided dbs
                pbc.fGammon = Integer.valueOf(new String(sz, 15, 1, "UTF-8")) != 0;
                pbc.fCompressed = Integer.valueOf(new String(sz, 17, 1, "UTF-8")) != 0;
                pbc.fND = Integer.valueOf(new String(sz, 19, 1, "UTF-8")) != 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        readBinaryData(pbc, szFilename);
        return pbc;
    }

    public void readBearoffData(int offset, byte[] buf, int nBytes) {
        System.arraycopy(data, offset, buf, 0, nBytes);
    }

    private static void readBinaryData(BearoffContext pbc, String szFilename) {
        try {
            pbc.data = IOUtils.toByteArray(BearoffContext.class.getResourceAsStream(szFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getnPoints() {
        return nPoints;
    }

    public int getnChequers() {
        return nChequers;
    }

    public boolean isfCubeful() {
        return fCubeful;
    }

    public boolean isTwoSided() {
        return isTwoSided;
    }

    public boolean isfND() {
        return fND;
    }

    public boolean isfGammon() {
        return fGammon;
    }

    public boolean isfCompressed() {
        return fCompressed;
    }

    public String getSzFilename() {
        return szFilename;
    }
}

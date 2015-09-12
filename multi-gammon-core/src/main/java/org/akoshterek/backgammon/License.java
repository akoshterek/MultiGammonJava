package org.akoshterek.backgammon;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class License {
    public static void printLicense() {
        printResourse("/org/akoshterek/backgammon/COPYING");
    }

    public static void printWarranty() {
        printResourse("/org/akoshterek/backgammon/WARRANTY");
    }

    public static void printBanner() {
        printResourse("/org/akoshterek/backgammon/BANNER");
    }

    private static void printResourse(String resourceName) {
        InputStream inputStream = License.class.getResourceAsStream(resourceName);
        try {
            IOUtils.copy(inputStream, System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

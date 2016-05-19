package org.akoshterek.backgammon.data;

import org.akoshterek.backgammon.Constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class TrainDataLoader {
    public static List<TrainEntry> loadData(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<TrainEntry> data = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s");
                TrainEntry entry = new TrainEntry();
                entry.positionId = tokens[0];
                for(int i = 0; i < Constants.NUM_OUTPUTS(); i++) {
                    entry.reward[i] = Double.parseDouble(tokens[i + 1]);
                }

                data.add(entry);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<TrainEntry> loadGzipResourceData(String resource) {
        try(GZIPInputStream is = new GZIPInputStream(TrainDataLoader.class.getResourceAsStream(resource))) {
            return loadData(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

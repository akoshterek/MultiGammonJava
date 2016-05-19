package org.akoshterek.backgammon.data;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionId;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.move.AuchKey;

import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Alex
 *         date 20.09.2015.
 */
public class DataPrepare {
    public static void main(String[] args) {
        String inputFileName = args[0];
        Map<String, Reward> trainingData = loadTrainingData(inputFileName);
        String outputFileName = replaceLast(inputFileName, ".gz", "-processed.gz");
        saveTrainingData(trainingData, outputFileName);
    }

    private static Map<String, Reward> loadTrainingData(String inputFileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFileName))))) {
            Map<String, Reward> data = new TreeMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("#")) {
                    continue;
                }

                String[] tokens = line.split("\\s");
                String positionId = PositionId.positionIDFromKey(AuchKey.fromNnPosition(tokens[0]));
                Reward reward = new Reward();
                for(int i = 0; i < Constants.NUM_OUTPUTS(); i++) {
                    reward.data[i] = Double.parseDouble(tokens[i + 1]);
                }

                data.put(positionId, reward);
            }

            addMissedInversions(data);
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveTrainingData(Map<String, Reward> data, String outputFileName) {
        try(PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(outputFileName)))) {
            data.forEach((positionId, reward) -> {
                String s = String.format(Locale.US, "%s %f %f %f %f %f", positionId,
                        reward.data[0],
                        reward.data[1],
                        reward.data[2],
                        reward.data[3],
                        reward.data[4]);
                writer.println(s);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addMissedInversions(Map<String, Reward> data) {
        Map<String, Reward> missed = new TreeMap<>();

        for(String key : data.keySet()) {
            Board board = Board.positionFromID(key);
            board.swapSides();
            String posRev = board.positionID();
            if(!data.containsKey(posRev)) {
                Reward reward = data.get(key);
                reward.invert();
                missed.put(posRev, reward);
            }
        }

        data.putAll(missed);
    }

    private static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }
}

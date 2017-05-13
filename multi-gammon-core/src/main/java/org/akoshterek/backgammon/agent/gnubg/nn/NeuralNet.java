package org.akoshterek.backgammon.agent.gnubg.nn;

import java.io.DataInput;
import java.io.IOException;

public class NeuralNet {
    private int cInput;
    private int cHidden;
    private int cOutput;
    private float rBetaHidden;
    private float rBetaOutput;
    private float []arHiddenWeight;
    private float []arOutputWeight;
    private float []arHiddenThreshold;
    private float []arOutputThreshold;

    private NeuralNet(int cInput, int cHidden, int cOutput, float rBetaHidden, float rBetaOutput) {
        this.cInput = cInput;
        this.cHidden = cHidden;
        this.cOutput = cOutput;
        this.rBetaHidden = rBetaHidden;
        this.rBetaOutput = rBetaOutput;

        arHiddenWeight = new float[cHidden * cInput];
        arOutputWeight = new float[cOutput * cHidden];
        arHiddenThreshold = new float[cHidden];
        arOutputThreshold = new float[cOutput];
    }

    public static NeuralNet loadBinary(DataInput inputStream)   {
        try {
            int cInput = inputStream.readInt();
            int cHidden = inputStream.readInt();
            int cOutput = inputStream.readInt();
            int nTrained = inputStream.readInt();
            float rBetaHidden = inputStream.readFloat();
            float rBetaOutput = inputStream.readFloat();

            if (cInput < 1 || cHidden < 1 || cOutput < 1 ||
                    nTrained < 0 || rBetaHidden <= 0.0 || rBetaOutput <= 0.0) {
                throw new IllegalArgumentException("Invalid NN file");
            }

            NeuralNet nn = new NeuralNet(cInput, cHidden, cOutput, rBetaHidden, rBetaOutput);
            readArray(nn.arHiddenWeight, inputStream);
            readArray(nn.arOutputWeight, inputStream);
            readArray(nn.arHiddenThreshold, inputStream);
            readArray(nn.arOutputThreshold, inputStream);
            return nn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void evaluate(double[] arInput, double[] arOutput) {
        float[] ar = new float[cHidden];

        // Calculate activity at hidden nodes
        System.arraycopy(arHiddenThreshold, 0, ar, 0, cHidden);

        int prWeight = 0;
        //arHiddenWeight
        for (int i = 0; i < cInput; i++) {
            float ari = (float)arInput[i];

            if (ari != 0) {
                int prIndex = 0;
                if (ari == 1.0f) {
                    for (int j = cHidden; j != 0; j--) {
                        ar[prIndex++] += arHiddenWeight[prWeight++];
                    }
                } else {
                    for (int j = cHidden; j != 0; j--) {
                        ar[prIndex++] += arHiddenWeight[prWeight++] * ari;
                    }
                }
            } else {
                prWeight += cHidden;
            }
        }

        for (int i = 0; i < cHidden; i++) {
            ar[i] = Sigmoid.sigmoid(-rBetaHidden * ar[i]);
        }

        // Calculate activity at output nodes
        prWeight = 0;
        //>arOutputWeight;

        for (int i = 0; i < cOutput; i++) {
            float r = arOutputThreshold[i];

            for (int j = 0; j < cHidden; j++)
                r += ar[j] * arOutputWeight[prWeight++];

            arOutput[i] = Sigmoid.sigmoid(-rBetaOutput * r);
        }
    }

    private static void readArray(float[] arr, DataInput inputStream) throws IOException {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = inputStream.readFloat();
        }
    }
}

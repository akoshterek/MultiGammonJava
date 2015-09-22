package org.akoshterek.backgammon.agent.inputrepresentation;

import org.akoshterek.backgammon.agent.raw.RawRepresentation;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class RepresentationFactory {
    public static InputRepresentation createInputRepresentation(String fullName) {
        String fullNameLower = fullName.toLowerCase();
        String[] tokens = fullNameLower.split("-");
        String representationName = tokens[0];
        String codecName = tokens[1];
        PointCodec codec = createCodec(codecName);
        switch (representationName) {
            case "raw":
                return new RawRepresentation(codec);
            default:
                throw new IllegalArgumentException("Unknown input representation " + representationName);
        }
    }

    private static PointCodec createCodec(String codecName) {
        switch (codecName) {
            case "sutton":
                return new SuttonCodec();
            case "tesauro89":
                return new Tesauro89Codec();
            case "tesauro92":
                return new Tesauro92Codec();
            case "gnubg":
                return new GnuBgCodec();
            default:
                throw new IllegalArgumentException("Unknown point codec " + codecName);
        }
    }
}

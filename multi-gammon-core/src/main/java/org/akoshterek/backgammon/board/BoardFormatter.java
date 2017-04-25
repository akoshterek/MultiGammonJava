package org.akoshterek.backgammon.board;

import org.akoshterek.backgammon.move.ChequersMove;

/**
 * @author Alex
 *         date 26.07.2015.
 */
public class BoardFormatter {
    public static String drawBoard(final Board board, final int fRoll, final String[] asz)
    {
        return drawBoardStd(board, fRoll, asz);
    }

    /*
     *  GNU Backgammon  Position ID: 0123456789ABCD
     *  +13-14-15-16-17-18------19-20-21-22-23-24-+     O: gnubg (Cube: 2)
     *  |                  |   | O  O  O  O     O | OO  0 points
     *  |                  |   | O     O          | OO  Cube offered at 2
     *  |                  |   |       O          | O
     *  |                  |   |                  | O
     *  |                  |   |                  | O
     * v|                  |BAR|                  |     Cube: 1 (7 point match)
     *  |                  |   |                  | X
     *  |                  |   |                  | X
     *  |                  |   |                  | X
     *  |                  |   |       X  X  X  X | X   Rolled 11
     *  |                  |   |    X  X  X  X  X | XX  0 points
     *  +12-11-10--9--8--7-------6--5--4--3--2--1-+     X: Gary (Cube: 2)
     *
     */
    private static String drawBoardStd(final Board board, final int fRoll, final String[] asz) {
        StringBuilder pch = new StringBuilder();
        int x, y;
        int cOffO = Board.TOTAL_MEN(), cOffX = Board.TOTAL_MEN();
        Board an = board.clone();
        int[][] anBoard = board.anBoard();

        String achX = "     X6789ABCDEF";
        String achO = "     O6789ABCDEF";

        for (x = 0; x < 25; x++) {
            cOffO -= anBoard[0][x];
            cOffX -= anBoard[1][x];
        }

        pch.append(String.format(" %-15s %s: ", "MultiGammon", "Position ID"));

        if (fRoll != 0) {
            pch.append(board.positionID());
        } else {
            an.swapSides();
            pch.append(an.positionID());
        }
        pch.append("              \n");

        pch.append(fRoll != 0 ? " +13-14-15-16-17-18------19-20-21-22-23-24-+     " :
                " +12-11-10--9--8--7-------6--5--4--3--2--1-+     ");
        pch.append("                    ");

        if (asz[0] != null) {
            pch.append(asz[0]);
        }
        pch.append('\n');

        for (y = 0; y < 4; y++) {
            pch.append(" |");

            for (x = 12; x < 18; x++) {
                pch.append(' ');
                pch.append(anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ');
                pch.append(' ');
            }

            pch.append("| ");
            pch.append(anBoard[0][24] > y ? 'O' : ' ');
            pch.append(" |");

            for (; x < 24; x++) {
                pch.append(' ');
                pch.append(anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ');
                pch.append(' ');
            }

            pch.append("| ");

            for (x = 0; x < 3; x++) {
                pch.append((cOffO > 5 * x + y) ? 'O' : ' ');
            }

            pch.append(' ');

            if (y < 2 && asz[y + 1] != null) {
                pch.append(asz[y + 1]);
            }
            pch.append('\n');
        }

        pch.append(" |");

        for (x = 12; x < 18; x++) {
            pch.append(' ');
            pch.append(anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) : achO.charAt(anBoard[0][23 - x]));
            pch.append(' ');
        }

        pch.append("| ");
        pch.append(achO.charAt(anBoard[0][24]));
        pch.append(" |");

        for (; x < 24; x++) {
            pch.append(' ');
            pch.append(anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]));
            pch.append(' ');
        }

        pch.append("| ");

        for (x = 0; x < 3; x++) {
            pch.append((cOffO > 5 * x + 4) ? 'O' : ' ');
        }

        pch.append('\n');

        pch.append(fRoll != 0 ? 'v' : '^');
        pch.append("|                  |BAR|                  |     ");
        //pch = strchr ( pch, 0 );

        if (asz[3] != null) {
            pch.append(asz[3]);
        }
        pch.append('\n');

        pch.append(" |");

        for (x = 11; x > 5; x--) {
            pch.append(' ');
            pch.append(anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]));
            pch.append(' ');
        }

        pch.append("| ");
        pch.append(achX.charAt(anBoard[1][24]));
        pch.append(" |");

        for (; x >= 0; x--) {
            pch.append(' ');
            pch.append(anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]));
            pch.append(' ');
        }

        pch.append("| ");

        for (x = 0; x < 3; x++) {
            pch.append((cOffX > 5 * x + 4) ? 'X' : ' ');
        }
        pch.append('\n');

        for (y = 3; y >= 0; y--) {
            pch.append(" |");

            for (x = 11; x > 5; x--) {
                pch.append(' ');
                pch.append(anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ');
                pch.append(' ');
            }

            pch.append("| ");
            pch.append(anBoard[1][24] > y ? 'X' : ' ');
            pch.append(" |");

            for (; x >= 0; x--) {
                pch.append(' ');
                pch.append(anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ');
                pch.append(' ');
            }

            pch.append("| ");

            for (x = 0; x < 3; x++)
                pch.append((cOffX > 5 * x + y) ? 'X' : ' ');

            pch.append(' ');

            if (y < 2 && asz[5 - y] != null) {
                pch.append(asz[5 - y]);
            }

            pch.append('\n');
        }

        pch.append(fRoll != 0 ? " +12-11-10--9--8--7-------6--5--4--3--2--1-+     " :
                " +13-14-15-16-17-18------19-20-21-22-23-24-+     ");
        pch.append("                    ");

        if (asz[6] != null) {
            pch.append(asz[6]);
        }

        return pch.toString();
    }

//    public static String fibsBoardShort(Board board) {
//        String pch = "";
//        int[] anOff = new int[] {0, 0};
//        byte[][] anBoard = board.anBoard;
//
//        // Opponent on bar
//        pch += String.format("%d:", -(int)anBoard[ 0 ][ 24 ] );
//
//        // Board
//        for(int i = 0; i < 24; i++ )
//        {
//            int point = (int)anBoard[ 0 ][ 23 - i ];
//            pch += String.format("%d:", (point > 0) ?  -point : (int)anBoard[ 1 ][ i ] );
//        }
//
//        // Player on bar
//        pch += String.format("%d:", anBoard[ 1 ][ 24 ] );
//
//        anOff[ 0 ] = anOff[ 1 ] = Board.TOTAL_MEN;
//        for(int i = 0; i < 25; i++ )
//        {
//            anOff[ 0 ] -= anBoard[ 0 ][ i ];
//            anOff[ 1 ] -= anBoard[ 1 ][ i ];
//        }
//
//        pch += String.format("%d:%d", anOff[ 1 ], -anOff[ 0 ]);
//        return pch;
//    }

    public static String formatMovePlain(final ChequersMove anMove, final Board anBoard)  {
        StringBuilder pch = new StringBuilder();
        int i, j;

        for (i = 0; i < 4 && anMove.move()[i].from() >= 0; i++) {
            pch.append(anMove.move()[i].toString());

            if (anMove.move()[i].to() >= 0 && anBoard.anBoard()[0][23 - anMove.move()[i].to()] != 0) {
                for (j = 1; ; j += 2) {
                    if (j > i) {
                        pch.append('*');
                        break;
                    } else if (anMove.move()[i + 1] == anMove.move()[j]) {
                        break;
                    }
                }
            }

            if( i < 6 ) {
                pch.append(' ');
            }
        }

        return pch.toString();
    }

    public static String formatPointPlain(final int n) {
        assert( n >= 0 );
        switch (n){
            case 25:
                return "bar";
            case 0:
                return "off";
            default:
                return String.format("%d", n);
        }
    }
}

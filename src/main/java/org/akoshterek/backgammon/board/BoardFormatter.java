package org.akoshterek.backgammon.board;

import org.akoshterek.backgammon.move.ChequerMove;

/**
 * @author Alex
 *         date 26.07.2015.
 */
public class BoardFormatter {
    public static String drawBoard(Board board, int fRoll, String[] asz)
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
    private static String drawBoardStd(Board board, int fRoll, String[] asz) {
        String pch = "";
        int x, y;
        int cOffO = Board.TOTAL_MEN, cOffX = Board.TOTAL_MEN;
        Board an = new Board(board);
        byte[][] anBoard = board.anBoard;

        String achX = "     X6789ABCDEF";
        String achO = "     O6789ABCDEF";

        for (x = 0; x < 25; x++) {
            cOffO -= anBoard[0][x];
            cOffX -= anBoard[1][x];
        }

        pch += String.format(" %-15s %s: ", "MultiGammon", "Position ID");

        if (fRoll != 0) {
            pch += board.positionID();
        } else {
            an.swapSides();
            pch += an.positionID();
        }
        pch += "              \n";

        pch += fRoll != 0 ? " +13-14-15-16-17-18------19-20-21-22-23-24-+     " :
                " +12-11-10--9--8--7-------6--5--4--3--2--1-+     ";
        pch += "                                                 ";

        if (asz[0] != null) {
            pch += asz[0];
        }
        pch += '\n';

        for (y = 0; y < 4; y++) {
            pch += ' ';
            pch += '|';

            for (x = 12; x < 18; x++) {
                pch += ' ';
                pch += anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ';
                pch += ' ';
            }

            pch += '|';
            pch += ' ';
            pch += anBoard[0][24] > y ? 'O' : ' ';
            pch += ' ';
            pch += '|';

            for (; x < 24; x++) {
                pch += ' ';
                pch += anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ';
                pch += ' ';
            }

            pch += '|';
            pch += ' ';

            for (x = 0; x < 3; x++) {
                pch += (cOffO > 5 * x + y) ? 'O' : ' ';
            }

            pch += ' ';

            if (y < 2 && asz[y + 1] != null) {
                pch += asz[y + 1];
            }
            pch += '\n';
        }

        pch += ' ';
        pch += '|';

        for (x = 12; x < 18; x++) {
            pch += ' ';
            pch += anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) : achO.charAt(anBoard[0][23 - x]);
            pch += ' ';
        }

        pch += '|';
        pch += ' ';
        pch += achO.charAt(anBoard[0][24]);
        pch += ' ';
        pch += '|';

        for (; x < 24; x++) {
            pch += ' ';
            pch += anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]);
            pch += ' ';
        }

        pch += '|';
        pch += ' ';

        for (x = 0; x < 3; x++) {
            pch += (cOffO > 5 * x + 4) ? 'O' : ' ';
        }

        pch += '\n';

        pch += fRoll != 0 ? 'v' : '^';
        pch += "|                  |BAR|                  |     ";
        //pch = strchr ( pch, 0 ); TODO: check

        if (asz[3] != null) {
            pch += asz[3];
        }
        pch += '\n';

        pch += ' ';
        pch += '|';

        for (x = 11; x > 5; x--) {
            pch += ' ';
            pch += anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]);
            pch += ' ';
        }

        pch += '|';
        pch += ' ';
        pch += achX.charAt(anBoard[1][24]);
        pch += ' ';
        pch += '|';

        for (; x >= 0; x--) {
            pch += ' ';
            pch += anBoard[1][x] != 0 ? achX.charAt(anBoard[1][x]) :
                    achO.charAt(anBoard[0][23 - x]);
            pch += ' ';
        }

        pch += '|';
        pch += ' ';

        for (x = 0; x < 3; x++) {
            pch += (cOffX > 5 * x + 4) ? 'X' : ' ';
        }
        pch += '\n';

        for (y = 3; y >= 0; y--) {
            pch += ' ';
            pch += '|';

            for (x = 11; x > 5; x--) {
                pch += ' ';
                pch += anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ';
                pch += ' ';
            }

            pch += '|';
            pch += ' ';
            pch += anBoard[1][24] > y ? 'X' : ' ';
            pch += ' ';
            pch += '|';

            for (; x >= 0; x--) {
                pch += ' ';
                pch += anBoard[1][x] > y ? 'X' :
                        anBoard[0][23 - x] > y ? 'O' : ' ';
                pch += ' ';
            }

            pch += '|';
            pch += ' ';

            for (x = 0; x < 3; x++)
                pch += (cOffX > 5 * x + y) ? 'X' : ' ';

            pch += ' ';

            if (y < 2 && asz[5 - y] != null) {
                pch += asz[5 - y];
            }

            pch += '\n';
        }

        pch += fRoll != 0 ? " +12-11-10--9--8--7-------6--5--4--3--2--1-+     " :
                " +13-14-15-16-17-18------19-20-21-22-23-24-+     ";
        pch += "                                                 ";

        if (asz[6] != null) {
            pch += asz[6];
        }

        pch += '\n';
        return pch;
    }

    public static String fibsBoardShort(Board board) {
        String pch = "";
        int[] anOff = new int[] {0, 0};
        byte[][] anBoard = board.anBoard;

        // Opponent on bar
        pch += String.format("%d:", -(int)anBoard[ 0 ][ 24 ] );

        // Board
        for(int i = 0; i < 24; i++ )
        {
            int point = (int)anBoard[ 0 ][ 23 - i ];
            pch += String.format("%d:", (point > 0) ?  -point : (int)anBoard[ 1 ][ i ] );
        }

        // Player on bar
        pch += String.format("%d:", anBoard[ 1 ][ 24 ] );

        anOff[ 0 ] = anOff[ 1 ] = Board.TOTAL_MEN;
        for(int i = 0; i < 25; i++ )
        {
            anOff[ 0 ] -= anBoard[ 0 ][ i ];
            anOff[ 1 ] -= anBoard[ 1 ][ i ];
        }

        pch += String.format("%d:%d", anOff[ 1 ], -anOff[ 0 ]);
        return pch;
    }

    public static String formatMovePlain(ChequerMove anMove, Board anBoard)  {
        String pch = "";
        int i, j;

        for( i = 0; i < 8 && anMove.move[ i ] >= 0; i += 2 ) {
            pch += formatPointPlain(anMove.move[ i ] + 1 );
            pch += '/';
            pch += formatPointPlain(anMove.move[i + 1] + 1);

            if(anMove.move[ i + 1 ] >= 0 &&
                    anBoard.anBoard[ 0 ][ 23 - anMove.move[ i + 1 ] ] != 0)       {
                for( j = 1; ; j += 2 )       {
                    if( j > i )               {
                        pch += '*';
                        break;
                    }
                    else
                    if( anMove.move[ i + 1 ] == anMove.move[ j ] ) {
                        break;
                    }
                }
            }

            if( i < 6 ) {
                pch += ' ';
            }
        }

        return pch;
    }

    private static String formatPointPlain(int n) {
        assert( n >= 0 );
        return String.format("%d", n);
    }


}

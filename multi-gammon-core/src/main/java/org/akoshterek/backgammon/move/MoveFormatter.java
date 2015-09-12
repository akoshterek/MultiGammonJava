package org.akoshterek.backgammon.move;

/**
 * @author Alex
 *         date 26.07.2015.
 */
public class MoveFormatter {
    private static String formatPoint(int n) {
        String pch = "";
        assert( n >= 0 );

        //don't translate 'off' and 'bar' as these may be used in UserCommand at a later point
        if( n ==0 )   {
            pch += "off";
            return pch;
        }
        else
        if( n == 25 )
        {
            pch += "bar";
            return pch;
        }
        else
        if( n > 9 ) {
            pch +=  (char)(n / 10 + '0');
        }

        pch += (char) (( n % 10 ) + '0');
        return pch;
    }

    private static String formatPointPlain(int n ) {
        String pch = "";
        assert( n >= 0 );

        if( n > 9 ) {
            pch +=  (char)(n / 10 + '0');
        }

        pch += (char) (( n % 10 ) + '0');
        return pch;
    }
}

package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionId;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date 26.08.2015.
 */
public class Sanitizer {
    public static void sanityCheck(Board board, Reward reward) {
        //TODO implement this
//        int i, j, nciq;
//        int  ac[ 2 ], anBack[ 2 ], anCross[ 2 ], anGammonCross[ 2 ],
//                anBackgammonCross[ 2 ], anMaxTurns[ 2 ];
//        boolean fContact;
//
//        if( reward.data[ OUTPUT_WIN ] < 0.0f ) {
//            reward.data[OUTPUT_WIN] = 0.0f;
//        }
//        else if( reward.data[ OUTPUT_WIN ] > 1.0f ) {
//            reward.data[OUTPUT_WIN] = 1.0f;
//        }
//
//        ac[ 0 ] = ac[ 1 ] = anBack[ 0 ] = anBack[ 1 ] = anCross[ 0 ] =
//                anCross[ 1 ] = anBackgammonCross[ 0 ] = anBackgammonCross[ 1 ] = 0;
//        anGammonCross[ 0 ] = anGammonCross[ 1 ] = 1;
//
//        for(j = 0; j < 2; j++ ) {
//            for(i = 0, nciq = 0; i < 6; i++ )
//                if( board.anBoard[ j ][ i ] != 0) {
//                    anBack[ j ] = i;
//                    nciq += board.anBoard[ j ][ i ];
//                }
//            ac[ j ] = anCross[ j ] = nciq;
//
//            for( i = 6, nciq = 0; i < 12; i++ )
//                if( board.anBoard[ j ][ i ] )
//                {
//                    anBack[ j ] = i;
//                    nciq += board.anBoard[ j ][ i ];
//                }
//            ac[ j ] += nciq;
//            anCross[ j ] += 2*nciq;
//            anGammonCross[ j ] += nciq;
//
//            for( i = 12, nciq = 0; i < 18; i++ )
//                if( board.anBoard[ j ][ i ] )
//                {
//                    anBack[ j ] = i;
//                    nciq += board.anBoard[ j ][ i ];
//                }
//            ac[ j ] += nciq;
//            anCross[ j ] += 3*nciq;
//            anGammonCross[ j ] += 2*nciq;
//
//            for( i = 18, nciq = 0; i < 24; i++ )
//                if( board.anBoard[ j ][ i ] )
//                {
//                    anBack[ j ] = i;
//                    nciq += board.anBoard[ j ][ i ];
//                }
//            ac[ j ] += nciq;
//            anCross[ j ] += 4*nciq;
//            anGammonCross[ j ] += 3*nciq;
//            anBackgammonCross[ j ] = nciq;
//
//            if( board.anBoard[ j ][ 24 ] )
//            {
//                anBack[ j ] = 24;
//                ac[ j ] += board.anBoard[ j ][ 24 ];
//                anCross[ j ] += 5 * board.anBoard[ j ][ 24 ];
//                anGammonCross[ j ] += 4 * board.anBoard[ j ][ 24 ];
//                anBackgammonCross[ j ] += 2 * board.anBoard[ j ][ 24 ];
//            }
//        }
//
//        fContact = anBack[ 0 ] + anBack[ 1 ] >= 24;
//
//        if( !fContact )
//        {
//            for( i = 0; i < 2; i++ )
//            {
//                if( anBack[ i ] < 6 && pbc1 )
//                {
//                    anMaxTurns[ i ] =
//                            MaxTurns( PositionId.positionBearoff(board.anBoard[i], pbc1 -> nPoints, pbc1 -> nChequers) );
//                }
//                else
//                {
//                    anMaxTurns[ i ] = anCross[ i ] * 2;
//                }
//            }
//
//            if ( ! anMaxTurns[ 1 ] ) anMaxTurns[ 1 ] = 1;
//        }
//
//        if( !fContact && anCross[ 0 ] > 4 * ( anMaxTurns[ 1 ] - 1 ) )
//        {
//            // Certain win
//            reward.data[ OUTPUT_WIN ] = 1.0f;
//        }
//
//        if( ac[ 0 ] < 15 )
//        {
//            // Opponent has borne off; no gammons or backgammons possible
//            reward.data[ OUTPUT_WINGAMMON ] = reward.data[ OUTPUT_WINBACKGAMMON ] = 0.0f;
//        }
//        else
//        if( !fContact )
//        {
//            if( anCross[ 1 ] > 8 * anGammonCross[ 0 ] )
//            {
//                // Gammon impossible
//                reward[ OUTPUT_WINGAMMON ] = 0.0f;
//            }
//            else
//            if( anGammonCross[ 0 ] > 4 * ( anMaxTurns[ 1 ] - 1 ) )
//            {
//                // Certain gammon
//                reward[ OUTPUT_WINGAMMON ] = 1.0f;
//            }
//
//            if( anCross[ 1 ] > 8 * anBackgammonCross[ 0 ] )
//            {
//                // Backgammon impossible
//                reward.data[ OUTPUT_WINBACKGAMMON ] = 0.0f;
//            }
//            else
//            if( anBackgammonCross[ 0 ] > 4 * ( anMaxTurns[ 1 ] - 1 ) )
//            {
//                // Certain backgammon
//                reward.data[ OUTPUT_WINGAMMON ] = reward.data[ OUTPUT_WINBACKGAMMON ] = 1.0f;
//            }
//        }
//
//        if( !fContact && anCross[ 1 ] > 4 * anMaxTurns[ 0 ] )
//        {
//            // Certain loss
//            reward[ OUTPUT_WIN ] = 0.0f;
//        }
//
//        if( ac[ 1 ] < 15 )
//        {
//            // Player has borne off; no gammon or backgammon losses possible
//            reward.data[ OUTPUT_LOSEGAMMON ] = reward.data[ OUTPUT_LOSEBACKGAMMON ] = 0.0f;
//        }
//        else if( !fContact )
//        {
//            if( anCross[ 0 ] > 8 * anGammonCross[ 1 ] - 4 )
//            {
//                // Gammon loss impossible
//                reward[ OUTPUT_LOSEGAMMON ] = 0.0f;
//            }
//            else
//            if( anGammonCross[ 1 ] > 4 * anMaxTurns[ 0 ] )
//            {
//                // Certain gammon loss
//                reward[ OUTPUT_LOSEGAMMON ] = 1.0f;
//            }
//
//            if( anCross[ 0 ] > 8 * anBackgammonCross[ 1 ] - 4 )
//            {
//                // Backgammon loss impossible
//                reward[ OUTPUT_LOSEBACKGAMMON ] = 0.0f;
//            }
//            else if( anBackgammonCross[ 1 ] > 4 * anMaxTurns[ 0 ] )
//            {
//                // Certain backgammon loss
//                reward[ OUTPUT_LOSEGAMMON ] = reward[ OUTPUT_LOSEBACKGAMMON ] = 1.0f;
//            }
//        }
//
//        // gammons must be less than wins
//        if( reward.data[ OUTPUT_WINGAMMON ] > reward.data[ OUTPUT_WIN ] )
//            reward.data[ OUTPUT_WINGAMMON ] = reward.data[ OUTPUT_WIN ];
//
//        double lose = 1.0 - reward.data[ OUTPUT_WIN ];
//        if( reward.data[ OUTPUT_LOSEGAMMON ] > lose )
//            reward.data[ OUTPUT_LOSEGAMMON ] = lose;
//
//        // Backgammons cannot exceed gammons
//        if( reward.data[ OUTPUT_WINBACKGAMMON ] > reward.data[ OUTPUT_WINGAMMON ] )
//            reward.data[ OUTPUT_WINBACKGAMMON ] = reward.data[ OUTPUT_WINGAMMON ];
//
//        if( reward.data[ OUTPUT_LOSEBACKGAMMON ] > reward.data[ OUTPUT_LOSEGAMMON ] )
//            reward.data[ OUTPUT_LOSEBACKGAMMON ] = reward.data[ OUTPUT_LOSEGAMMON ];
//
//        double noise = 1/10000.0f;
//        for(i = OUTPUT_WINGAMMON; i < OUTPUT_EQUITY; i++) {
//            if( reward.data[i] < noise )
//                reward.data[i] = 0;
//        }
    }
}

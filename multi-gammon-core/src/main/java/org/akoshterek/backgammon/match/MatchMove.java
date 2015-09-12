package org.akoshterek.backgammon.match;

import org.akoshterek.backgammon.move.MoveRecord;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alex
 *         date 06.08.2015.
 */
public class MatchMove {
    public final Deque<MoveRecord> moveRecords = new LinkedList<>();
}

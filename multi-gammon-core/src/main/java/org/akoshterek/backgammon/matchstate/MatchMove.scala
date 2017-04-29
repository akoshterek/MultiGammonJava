package org.akoshterek.backgammon.matchstate

import org.akoshterek.backgammon.move.MoveRecord

import scala.collection.mutable

/**
  * @author Alex
  *         date 06.08.2015.
  */
final class MatchMove {
  val moveRecords: mutable.Buffer[MoveRecord] = mutable.Buffer[MoveRecord]()
}
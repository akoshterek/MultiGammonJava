package org.akoshterek.backgammon.agent.inputrepresentation

trait PointCodec {
  /**
    * @return amount of inputs per point
    */
  def inputsPerPoint: Int = 4

  /**
    * @param men   amount of men on the point
    * @param index in range [0, inputsPerPoint)
    * @return encoded input
    */
  def point(men: Int, index: Int): Double

  def self: this.type = this
}
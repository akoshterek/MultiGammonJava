package org.akoshterek.backgammon.agent.raw

/**
  * Created by Alex on 27-05-17.
  */
class RawStat(val name: String,
              val epochContact: Int,
              val errorContact: Double,
              val epochCrashed: Int,
              val errorCrashed: Double,
              val epochRace: Int,
              val errorRace: Double) {

}

object RawStat {
  def apply(name: String,
            epochContact: Int,
            errorContact: Double,
            epochCrashed: Int,
            errorCrashed: Double,
            epochRace: Int,
            errorRace: Double): RawStat = {
    new RawStat(name, epochContact, errorContact, epochCrashed, errorCrashed, epochRace, errorRace)
  }

  val stat: Vector[RawStat] = Vector[RawStat](
    RawStat("Raw-Sutton-40-sigmoid", 297, 0.01125464, 462, 0.017861445, 495, 0.01493589),
    RawStat("Raw-Tesauro89-40-sigmoid", 462, 0.010316408, 671, 0.017121587, 319, 0.019095553),
    RawStat("Raw-Tesauro92-40-sigmoid", 484, 0.011212102, 440, 0.018757652, 385, 0.017453306),
    RawStat("Raw-Gnubg-40-sigmoid", 407, 0.010867225, 440, 0.020192931, 286, 0.018877601),
    RawStat("Raw-Sutton-40-tanh", 231, 0.009976033, 132, 0.016928241, 253, 0.014361802),
    RawStat("Raw-Tesauro89-40-tanh", 88, 0.009661042, 88, 0.0174888, 77, 0.016881599),
    RawStat("Raw-Tesauro92-40-tanh", 165, 0.00958327, 132, 0.01924929, 88, 0.017262174),
    RawStat("Raw-Gnubg-40-tanh", 143, 0.009245077, 110, 0.01773336, 99, 0.01843053),
    RawStat("Raw-Sutton-40-relu", 462, 0.009106555, 318, 0.018416477, 297, 0.006401024),
    RawStat("Raw-Tesauro89-40-relu", 176, 0.009606434, 198, 0.014302652, 209, 0.012420331),
    RawStat("Raw-Tesauro92-40-relu", 484, 0.011201971, 264, 0.013260297, 165, 0.016273506),
    RawStat("Raw-Gnubg-40-relu", 198, 0.008239474, 176, 0.015525941, 253, 0.014070139),
    RawStat("Raw-Sutton-40-lrelu", 319, 0.010033395, 352, 0.031170188, 308, 0.017889299),
    RawStat("Raw-Tesauro89-40-lrelu", 165, 0.008594542, 187, 0.014490089, 165, 0.011670147),
    RawStat("Raw-Tesauro92-40-lrelu", 165, 0.009164988, 254, 0.014580407, 198, 0.013881747),
    RawStat("Raw-Gnubg-40-lrelu", 220, 0.009193579, 253, 0.01460083, 143, 0.017127941),
    RawStat("Raw-Sutton-40-elu", 286, 0.010007333, 297, 0.018636813, 374, 0.06161806),
    RawStat("Raw-Tesauro89-40-elu", 209, 0.00937956, 187, 0.01525118, 187, 0.015201897),
    RawStat("Raw-Tesauro92-40-elu", 264, 0.012175463, 341, 0.01655333, 154, 0.009857578),
    RawStat("Raw-Gnubg-40-elu", 209, 0.010999638, 165, 0.016480745, 264, 0.015522892),
    RawStat("Raw-Sutton-40-softplus", 847, 0.010939108, 693, 0.018173033, 572, 0.014006384),
    RawStat("Raw-Tesauro89-40-softplus", 605, 0.008986193, 451, 0.015653644, 198, 0.020338967),
    RawStat("Raw-Tesauro92-40-softplus", 451, 0.0074502, 253, 0.083747762, 682, 0.010429162),
    RawStat("Raw-Gnubg-40-softplus", 374, 0.013592015, 462, 0.014219883, 308, 0.023133804),
    RawStat("Raw-Sutton-40-elliot", 165, 0.0088342, 231, 0.016164361, 154, 0.020410544),
    RawStat("Raw-Tesauro89-40-elliot", 110, 0.009194724, 110, 0.017402068, 88, 0.015310367),
    RawStat("Raw-Tesauro92-40-elliot", 165, 0.008828014, 132, 0.01733417, 132, 0.015260288),
    RawStat("Raw-Gnubg-40-elliot", 110, 0.009519367, 165, 0.017271867, 88, 0.015029309),
    RawStat("Raw-Sutton-40-log", 440, 0.009952604, 462, 0.015462061, 330, 0.015599555),
    RawStat("Raw-Tesauro89-40-log", 231, 0.008268482, 242, 0.01598384, 209, 0.014731648),
    RawStat("Raw-Tesauro92-40-log", 297, 0.008241896, 308, 0.015832933, 308, 0.013610445),
    RawStat("Raw-Gnubg-40-log", 220, 0.009025399, 264, 0.016487361, 198, 0.013197762)
  )
}
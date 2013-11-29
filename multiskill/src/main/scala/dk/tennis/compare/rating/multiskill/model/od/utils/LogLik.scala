package dk.tennis.compare.rating.multiskill.model.od.utils

import scala.math._

object LogLik {

  /**
   * @param predictions Seq of [predicted prob, points won, points total]
   *
   * Returns [total,avg] log likelihood
   */
  def logLik(predictions: Seq[Tuple3[Double, Int, Int]]): Tuple2[Double, Double] = {

    val totalLoglik = predictions.foldLeft(0d) { (totalLoglik, p) =>

      val (pointProb,pointsWon, pointsTotal) = p
      val logLik = pointsWon * log(pointProb) + (pointsTotal - pointsWon) * log1p(-pointProb)
      totalLoglik + logLik
    }

    val totalPoints = predictions.map(p => p._3).sum
    val avgLogLik = totalLoglik / totalPoints

    (totalLoglik, avgLogLik)
  }
}
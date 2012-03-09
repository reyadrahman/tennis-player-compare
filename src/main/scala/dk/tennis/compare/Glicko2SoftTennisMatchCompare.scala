package dk.tennis.compare
import java.util.Date
import dk.atp.api.domain.SurfaceEnum._
import dk.tennisprob.TennisProbCalc.MatchTypeEnum._
import dk.atp.api.ATPMatchesLoader
import org.joda.time._
import dk.atp.api.domain.MatchComposite
import glicko.GlickoRating._
import dk.tennis.compare.glicko.GenericGlickoRating
import dk.tennisprob._
import glicko2._

class Glicko2SoftTennisMatchCompare(glicko2RatingLoader: Glicko2RatingsLoader) extends TennisPlayerCompare {

  /**
   * Calculates probability of winning a tennis match by player A against player B.
   *
   * @param fullNamePlayerA e.g. Roger Federer
   * @param fullNamePlayerB e.g. Novak Djokovic
   * @param surface Clay, grass or hard.
   * @param matchType Three or five set match.
   * @param marketTime When the tennis match was played.
   *
   * @return Probability between 0 and 1.
   */
  def matchProb(fullNamePlayerA: String, fullNamePlayerB: String, surface: SurfaceEnum, matchType: MatchTypeEnum, marketTime: Date): Double = {

    val ratings = glicko2RatingLoader.ratings(marketTime, surface)

    val playerARating = ratings(fullNamePlayerA)
    val playerBRating = ratings(fullNamePlayerB)
    val playerAOnServeProb = GenericGlicko2Rating.E(playerARating.ratingOnServe.rating, playerBRating.ratingOnReturn.rating, playerBRating.ratingOnReturn.deviation)
    val playerBOnServeProb = GenericGlicko2Rating.E(playerBRating.ratingOnServe.rating, playerARating.ratingOnReturn.rating, playerARating.ratingOnReturn.deviation)

    val matchProbAGivenB = TennisProbFormulaCalc.matchProb(playerAOnServeProb, 1 - playerBOnServeProb, matchType)
    matchProbAGivenB
  }

}
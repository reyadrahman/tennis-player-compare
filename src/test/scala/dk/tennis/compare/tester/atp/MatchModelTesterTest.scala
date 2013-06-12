package dk.tennis.compare.tester.atp

import scala.io.Source
import org.joda.time.DateTime
import org.junit.Test
import org.slf4j.LoggerFactory
import dk.atp.api.CSVATPMatchesLoader
import dk.atp.api.domain.SurfaceEnum.HARD
import dk.tennis.compare.game.tennis.domain.BfMarket
import dk.tennis.compare.game.tennis.domain.TennisResult
import dk.tennis.compare.game.tennis.model.ExPricesMatchModel
import dk.tennis.compare.game.tennis.model.Glicko2MatchModel
import dk.tennis.compare.game.tennis.model.PointStatsMatchModel
import dk.tennis.compare.game.tennis.model.TrueSkillExPriceModel
import dk.tennis.compare.game.tennis.model.TrueSkillMatchModel
import dk.tennis.compare.tester.GameModelTester
import dk.tennis.compare.tester.GameResult
import java.util.Date
import scala.util.Random
import dk.tennis.compare.game.tennis.domain.TennisPoint

class MatchModelTesterTest {

  val log = LoggerFactory.getLogger(getClass)

  val matchesFile = "./src/test/resources/atp_historical_data/match_data_2006_2011.csv"
  val atpMatches = getAtpMatches(matchesFile, 2011, 2011)

  val tester = GameModelTester(atpMatches)

  @Test def test {

    val marketDataSource = Source.fromFile("./src/test/resources/betfair_data/betfair_data_tennis_mens_2010_2011_less_than_30m_before_kick_off.csv")
    val bfMarkets = BfMarket.fromCSV(marketDataSource.getLines().drop(1).toList)
    val exModel = ExPricesMatchModel(atpMatches, bfMarkets)

    val glicko2Model = Glicko2MatchModel()
    //    val model = Glicko2HardMatchModel()
    val trueSkillModel = TrueSkillMatchModel()
    //  val model = TrueSkillDBNMatchModel()
    // val model = TrueSkillGlicko2MatchModel()

    val trueSkillExModel = TrueSkillExPriceModel(trueSkillModel, exModel)

    val pointStatModel = PointStatsMatchModel()

    val matchFilter = (m: GameResult) => {
      m match {
        case m: TennisResult => {
          log.info(new DateTime(m.timestamp.get).toString() + ". Log likelihood stats = " + tester.getLlhStats());
          new DateTime(m.timestamp.get).getYear() >= 2007
        }
      }
    }
//        val matchFilter = (m: GameResult) => {
//          new DateTime(m.timestamp.get).getYear() >=2007 &&
//            (m.containsPlayer("Roger Federer"))
//        }

    val modelSummary = tester.run(trueSkillExModel, matchFilter)

    val probs = modelSummary.predictionRecords.filter(r => r.playerAWinnerProb > 0.5).map(r => if (r.playerAWinner == 1) r.playerAWinnerProb else (1 - r.playerAWinnerProb))

    //println("win avg prob= " + probs.sum / probs.size)

    log.info("Log likelihood stats = " + modelSummary.llhStats)
    log.info("Prediction error = " + modelSummary.predictionError())
    log.info("players number = " + modelSummary.playersNum)
    log.info("Expected/actual wins: %.3f/%s".format(modelSummary.playerAExpectedWins, modelSummary.playerActualWins))

    //  log.info(modelSummary.predictedActualAvgCorrReport)
  }

  private def getAtpMatches(matchesFile: String, yearFrom: Int, yearTo: Int): Seq[TennisResult] = {
    val atpMatchesLoader = CSVATPMatchesLoader.fromCSVFile(matchesFile)

    val matches = (yearFrom to yearTo).flatMap(year => atpMatchesLoader.loadMatches(year))
    val filteredMatches = matches.filter(m => (m.tournament.surface == HARD) && m.matchFacts.playerAFacts.totalServicePointsWon > 10 && m.matchFacts.playerBFacts.totalServicePointsWon > 10)

    val gameResults = TennisResult.fromMatches(filteredMatches)
    gameResults
  }
}
package dk.tennis.compare.rating.multiskill.factorgraph

import scala.math.pow
import scala.util.Random
import org.junit.Test
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import dk.atp.api.CSVATPMatchesLoader
import dk.atp.api.domain.SurfaceEnum.HARD
import dk.bayes.infer.ep.GenericEP
import dk.tennis.compare.game.tennis.domain.TennisResult
import dk.bayes.infer.ep.calibrate.fb.ForwardBackwardEPCalibrate
import org.apache.commons.lang.time.StopWatch
import dk.tennis.compare.rating.multiskill.domain.PointResult
import dk.tennis.compare.rating.multiskill.domain.MatchResult
import dk.tennis.compare.rating.multiskill.testutil.MultiSkillTestUtil._
import dk.tennis.compare.rating.multiskill.domain.MultiSkillParams
import dk.tennis.compare.rating.multiskill.domain.PlayerSkill

class TennisDbnFactorGraphTest {

  val logger = Logger(LoggerFactory.getLogger(getClass()))

  val matchResults = loadTennisMatches(2011, 2011)

  val multiSkillParams = MultiSkillParams(
    skillOnServeTransVariance = 0.02,
    skillOnReturnTransVariance = 0.02,
    priorSkillOnServe = PlayerSkill(0, 1), priorSkillOnReturn = PlayerSkill(0, 1),
    perfVariance = 200)

  @Test def calibrate {

    println("Results num:" + matchResults.size)

    val tennisFactorGraph = TennisDbnFactorGraph(multiSkillParams)

    matchResults.foreach(r => tennisFactorGraph.addTennisMatch(r))

    println("Factors num: " + tennisFactorGraph.getFactorGraph.getFactorNodes.size)
    println("Variables num: " + tennisFactorGraph.getFactorGraph.getVariables.size)

    val timer = new StopWatch()
    timer.start()

    val epCalibrate = ForwardBackwardEPCalibrate(tennisFactorGraph.getFactorGraph())
    val iterTotal = epCalibrate.calibrate(100, progress)
    logger.debug("Iter total: " + iterTotal)
    logger.debug("Time: " + timer.getTime())
    //  assertEquals(EPSummary(8, 87936), iterTotal)

    val ep = GenericEP(tennisFactorGraph.getFactorGraph())
    val varIdsOnServe = tennisFactorGraph.getSkillVarIdsOnServe()("Roger Federer")
    varIdsOnServe.takeRight(1).foreach(vId => println("Roger Federer on serve:" + ep.marginal(vId)))

    val varIdsOnReturn = tennisFactorGraph.getSkillVarIdsOnReturn()("Roger Federer")
    varIdsOnReturn.takeRight(1).foreach(vId => println("Roger Federer on return:" + ep.marginal(vId)))

  }

  private def progress(currIter: Int) = println("EP iteration: " + currIter)
}
package dk.tennis.compare.rating.multiskill.model.multipoint

import org.junit._
import Assert._
import dk.tennis.compare.rating.multiskill.domain.PlayerSkill
import dk.tennis.compare.rating.multiskill.testutil.MultiSkillTestUtil._

class GenericMultiPointModelTest {

  @Test def test {

    val p1Skill = PlayerSkill(-1, 1)
    val p2Skill = PlayerSkill(0.5, 1.2)

    val p1PerfVariance = 190
    val p2PerfVariance = 170
    val model = GenericMultiPointModel(p1PerfVariance, p2PerfVariance)

    val (newP1Skill, newP2Skill, factorGraph) = model.skillMarginals(p1Skill, p2Skill, 7400, 10000)

    assertPlayerSkill(PlayerSkill(5.0527, 0.0613), newP1Skill, 0.0001)
    assertPlayerSkill(PlayerSkill(-6.7624, 0.0620), newP2Skill, 0.0001)

  }

}
/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 2026-03-03 |||||||||||*/

package planning.engine.planner.config

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecWithData

class PlannerMapConfigSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val rawConfig: String =
      """
        |planner {
        |  map {
        |    repr-enabled = true
        |  }
        |}
      """.stripMargin

    val parsedConfig: PlannerMapConfig = PlannerMapConfig(
      reprEnabled = true
    )

  "PlannerMapConfig.formConfig(...)" should:
    "load configuration successfully" in newCase[CaseData]: (_, data) =>
      PlannerMapConfig
        .formConfig[IO](ConfigFactory.parseString(data.rawConfig).getConfig("planner.map"))
        .asserting(_ mustEqual data.parsedConfig)

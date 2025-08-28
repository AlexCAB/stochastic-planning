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
| created: 2025-08-26 |||||||||||*/

package planning.engine.planner.config

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecWithData

class SimpleSyncPlannerConfigSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val rawConfig: String =
      """
        |planner {
        |  max-context-path-length = 100
        |}
      """.stripMargin

    val parsedConfig: SimpleSyncPlannerConfig = SimpleSyncPlannerConfig(
      maxContextPathLength = 100
    )

  "SimpleSyncPlannerConfig.formConfig(...)" should:
    "load configuration successfully" in newCase[CaseData]: (_, data) =>
      SimpleSyncPlannerConfig
        .formConfig[IO](ConfigFactory.parseString(data.rawConfig).getConfig("planner"))
        .asserting(_ mustEqual data.parsedConfig)

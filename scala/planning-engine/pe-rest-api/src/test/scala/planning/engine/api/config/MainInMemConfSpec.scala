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
| created: 2025-12-27 |||||||||||*/

package planning.engine.api.config

import cats.effect.IO
import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.config.MapVisualizationConfig

import scala.concurrent.duration.DurationInt

class MainInMemConfSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val validConfig = ConfigFactory.parseString(
      """
        |api.server {
        |  host = "127.0.0.1"
        |  port = 8080
        |  api-prefix = "/api"
        |}
        |planner.map.visualization {
        |  enabled = false
        |  long-pull-timeout = 1 minute
        |}
        |""".stripMargin
    )

  "MainInMemCon.formConfig(...)" should:
    "load MainInMemConf from valid configuration" in newCase[CaseData]: (tn, data) =>
      MainInMemConf.formConfig[IO](data.validConfig)
        .logValue(tn, "MainInMemConf")
        .asserting(_ mustEqual MainInMemConf(
          server = ServerConf(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get, "/api"),
          visualization = MapVisualizationConfig(enabled = false, longPullTimeout = 1.minute)
        ))

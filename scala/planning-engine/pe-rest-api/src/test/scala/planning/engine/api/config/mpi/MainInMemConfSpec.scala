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
| created: 2026-09-26 |||||||||||*/

package planning.engine.api.config.mpi

import cats.effect.IO
import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.{Config, ConfigFactory}
import planning.engine.api.config.mpi.MainInMemConf
import planning.engine.api.config.parts.{ServerConf, VisualizationRouteConf}
import planning.engine.common.UnitSpecWithData

import scala.concurrent.duration.DurationInt

class MainInMemConfSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val validConfig: Config = ConfigFactory.parseString(
      """
        |api.server {
        |  host = "127.0.0.1"
        |  port = 8080
        |  api-prefix = "/api"
        |}
        |api.route.visualization {
        |  ping-timeout = 1 minute
        |}
        |api.visualization.enabled = true
        |""".stripMargin,
    )

  "MainInMemCon.formConfig(...)" should:
    "load MainInMemConf from valid configuration" in newCase[CaseData]: (tn, data) =>
      MainInMemConf.formConfig[IO](data.validConfig)
        .logValue(tn, "MainInMemConf")
        .asserting(_ mustEqual MainInMemConf(
          server = ServerConf(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get, "/api"),
          visRoute = VisualizationRouteConf(pingTimeout = 1.minute),
          visEnabled = true,
        ))

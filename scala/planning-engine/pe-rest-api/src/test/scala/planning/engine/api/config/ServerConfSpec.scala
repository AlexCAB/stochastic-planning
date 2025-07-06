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
| created: 2025-04-25 |||||||||||*/

package planning.engine.api.config

import cats.effect.IO
import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecWithData

class ServerConfSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val validConfig = ConfigFactory.parseString(
      """
        |host = "127.0.0.1"
        |port = 8080
        |api-prefix = "/api"
        |""".stripMargin
    )

  "ServerConf.formConfig(...)" should:
    "create ServerConf from valid configuration" in newCase[CaseData]: (tn, data) =>
      ServerConf.formConfig[IO](data.validConfig)
        .logValue(tn)
        .asserting(_ mustEqual ServerConf(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get, "/api"))

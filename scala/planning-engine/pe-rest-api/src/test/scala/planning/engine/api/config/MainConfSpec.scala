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
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecIO
import planning.engine.common.config.Neo4jConnectionConf
import com.comcast.ip4s.{Host, Port}

class MainConfSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val validConfig = ConfigFactory.parseString(
      """
        |db {
        |  neo4j {
        |    uri = "bolt://localhost:7687"
        |    user = "neo4j"
        |    password = "password"
        |  }
        |  name = "testDatabase"
        |}
        |api.server {
        |  host = "127.0.0.1"
        |  port = 8080
        |  api-prefix = "/api"
        |}
        |""".stripMargin
    )

  "default" should:
    "load MainConf from valid configuration" in newCase[CaseData]: data =>
      MainConf.formConfig[IO](data.validConfig)
        .logValue
        .asserting(_ mustEqual MainConf(
          DbConf(
            Neo4jConnectionConf(uri = "bolt://localhost:7687", user = "neo4j", password = "password"),
            "testDatabase"
          ),
          ServerConf(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get, "/api")
        ))

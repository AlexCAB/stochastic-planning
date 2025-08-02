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
import planning.engine.common.UnitSpecWithData
import planning.engine.map.database.Neo4jConf
import com.comcast.ip4s.{Host, Port}
import planning.engine.map.graph.MapConfig

class MainConfSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val validConfig = ConfigFactory.parseString(
      """
        |db {
        |  neo4j {
        |    uri = "bolt://localhost:7687"
        |    user = "neo4j"
        |    password = "password"
        |  }
        |}
        |api.server {
        |  host = "127.0.0.1"
        |  port = 8080
        |  api-prefix = "/api"
        |}
        |map-graph {
        |  init-next-hn-id = 1
        |  init-next-sample-id = 2
        |  init-sample-count = 3
        |  init-next-hn-index = 4
        |}
        |""".stripMargin
    )

  "MainConf.formConfig(...)" should:
    "load MainConf from valid configuration" in newCase[CaseData]: (tn, data) =>
      MainConf.formConfig[IO](data.validConfig)
        .logValue(tn, "MainConf")
        .asserting(_ mustEqual MainConf(
          db = DbConf(Neo4jConf(uri = "bolt://localhost:7687", user = "neo4j", password = "password")),
          server = ServerConf(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get, "/api"),
          mapGraph = MapConfig(
            initNextHnId = 1L,
            initNextSampleId = 2L,
            initSampleCount = 3L,
            initNextHnIndex = 4L
          )
        ))

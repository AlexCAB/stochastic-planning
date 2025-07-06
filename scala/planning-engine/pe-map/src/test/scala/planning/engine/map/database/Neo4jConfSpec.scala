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
| created: 2025-03-10 |||||||||||*/

package planning.engine.map.database

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import planning.engine.common.UnitSpecIO

class Neo4jConfSpec extends UnitSpecIO:

  private val configStr: String =
    """
      |neo4j {
      |  user = "testUser"
      |  password = "testPassword"
      |  uri = "neo4j://localhost:7687"
      |}
      |""".stripMargin

  "Neo4jConf.formConfig(...)" should:
    "load configuration successfully" in: _ =>
      val config: Config = ConfigFactory.parseString(configStr).getConfig("neo4j")

      Neo4jConf
        .formConfig[IO](config)
        .asserting(_ mustEqual Neo4jConf("testUser", "testPassword", "neo4j://localhost:7687"))

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

package planning.engine.common.config

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import planning.engine.common.UnitSpecIO

class Neo4jConnectionConfSpec extends UnitSpecIO:

  private val configStr: String =
    """
      |neo4j {
      |  user = "testUser"
      |  password = "testPassword"
      |  uri = "neo4j://localhost:7687"
      |}
      |""".stripMargin

  "formConfig" should:
    "load configuration successfully" in:
      val config: Config = ConfigFactory.parseString(configStr).getConfig("neo4j")

      Neo4jConnectionConf
        .formConfig[IO](config)
        .asserting(_ mustEqual Neo4jConnectionConf("testUser", "testPassword", "neo4j://localhost:7687"))

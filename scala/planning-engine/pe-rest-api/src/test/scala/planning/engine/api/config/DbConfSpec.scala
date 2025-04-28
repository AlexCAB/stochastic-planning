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

class DbConfSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val validConfig = ConfigFactory.parseString(
      """
        |neo4j {
        |  uri = "bolt://localhost:7687"
        |  user = "neo4j"
        |  password = "password"
        |}
        |name = "testDatabase"
        |""".stripMargin
    )

  "formConfig" should:
    "create DbConf from valid configuration" in newCase[CaseData]: data =>
      DbConf.formConfig[IO](data.validConfig)
        .logValue
        .asserting(_ mustEqual DbConf(
          Neo4jConnectionConf(uri = "bolt://localhost:7687", user = "neo4j", password = "password"),
          "testDatabase"
        ))

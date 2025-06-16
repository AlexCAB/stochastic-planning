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

class DbConfSpec extends UnitSpecWithData:

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
    "create DbConf from valid configuration" in newCase[CaseData]: (tn, data) =>
      DbConf.formConfig[IO](data.validConfig)
        .logValue(tn)
        .asserting(_ mustEqual DbConf(
          Neo4jConf(uri = "bolt://localhost:7687", user = "neo4j", password = "password"),
          "testDatabase"
        ))

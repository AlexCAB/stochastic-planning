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
import cats.effect.unsafe.implicits.global
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification



class Neo4jConnectionConfSpec extends Specification:

  "formConfig" should:
    "load configuration successfully" in:
      val configStr =
        """
          |neo4j {
          |  user = "testUser"
          |  password = "testPassword"
          |  uri = "neo4j://localhost:7687"
          |}
          |""".stripMargin
        
      val config = ConfigFactory.parseString(configStr).getConfig("neo4j")
      val result = Neo4jConnectionConf.formConfig[IO](config).unsafeRunSync()
      
      result must beEqualTo(Neo4jConnectionConf("testUser", "testPassword", "neo4j://localhost:7687"))

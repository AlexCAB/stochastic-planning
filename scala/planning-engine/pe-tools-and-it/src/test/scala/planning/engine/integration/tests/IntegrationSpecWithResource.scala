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
| created: 2025-03-24 |||||||||||*/

package planning.engine.integration.tests

import cats.effect.IO
import org.typelevel.log4cats.Logger
import planning.engine.common.UnitSpecWithResource

abstract class IntegrationSpecWithResource[R] extends UnitSpecWithResource[R]:

  override def afterAll(): Unit =
    super.afterAll()
    Thread.sleep(3000) // Wait for all resources to be released
    Logger[IO].info("All tests completed.").unsafeRunSync()

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
| created: 09.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import planning.engine.common.UnitSpecWithData
import org.scalatest.BeforeAndAfterAll

class UnitSpecWithIOAndTestKit extends UnitSpecWithData with BeforeAndAfterAll:
  val testKit = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

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
import org.apache.pekko.actor.typed.{ActorSystem, Scheduler}
import org.scalatest.BeforeAndAfterAll
import planning.engine.common.UnitSpecWithData

class UnitSpecWithIOAndTestKit extends UnitSpecWithData with BeforeAndAfterAll:
  given testKit: ActorTestKit = ActorTestKit()
  given system: ActorSystem[Nothing] = testKit.system
  given scheduler: Scheduler = system.scheduler
  override def afterAll(): Unit = testKit.shutdownTestKit()

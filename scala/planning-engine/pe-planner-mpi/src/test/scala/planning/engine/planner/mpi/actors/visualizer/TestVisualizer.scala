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
| created: 14.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import cats.effect.IO
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit

import java.util.concurrent.atomic.AtomicInteger

trait TestVisualizer:
  self: UnitSpecWithIOAndTestKit =>

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  trait WithVisualizerActor:
    def makeVisualizerActor(): Visualizer = Visualizer
      .spawn[IO]((bh, name) => testKit.spawn(bh, s"test-visualizer-$name-${nameIdCounter.getAndIncrement()}"))
      .unsafeRunSync()

    lazy val visualizer: Visualizer = makeVisualizerActor()
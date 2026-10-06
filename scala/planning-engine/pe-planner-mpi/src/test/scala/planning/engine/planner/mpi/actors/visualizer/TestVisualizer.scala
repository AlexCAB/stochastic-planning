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
| created: 15.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.visualizer.data.Definition
import planning.engine.planner.mpi.actors.visualizer.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.model.io.IoVars

import java.util.concurrent.atomic.AtomicInteger

final case class TestVisualizer(api: Visualizer):
  import TestVisualizer.*

  def ref: ActorRef[Visualizer.Msg] = api.ref

object TestVisualizer extends TestActorBase:
  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  def apply(vars: IoVars, viz: Visualization, name: String)(using tk: ActorTestKit): TestVisualizer =
    def spawn(bh: Behavior[Visualizer.Msg], name: String): ActorRef[Visualizer.Msg] =
      tk.spawn(bh, s"test-visualizer-$name-${nameIdCounter.getAndIncrement()}")

    new TestVisualizer(api = ApiImpl(Actor.spawn(Definition(vars, viz), spawn)))

  extension (api: Visualizer)
    def ref: ActorRef[Visualizer.Msg] = api match
      case ApiImpl(ref) => ref

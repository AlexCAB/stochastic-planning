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
| created: 15-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.graph.edges.MeKey
import org.apache.pekko.actor.typed.Behavior
import java.util.concurrent.atomic.AtomicInteger
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.visualizer.data.State
import planning.engine.planner.mpi.actors.visualizer.logic.ApiImpl

final case class TestVisualizer(api: Visualizer):
  import TestVisualizer.*
  
  def ref: ActorRef[Visualizer.Msg] = api.ref
  def state(using testKit: ActorTestKit): State = api.state
  def stateTyped(using ActorTestKit): VisualizerState = api.stateTyped

object TestVisualizer extends TestActorBase:
  type VisualizerState = (
      Map[MnId.Con, Option[HnName]],
      Map[MnId.Abs, Option[HnName]],
      Map[MnId, Set[MeKey.Link.End]],
      Map[MnId, Set[MeKey.Then.End]],
      Map[MnId, Set[MeKey.Link.End]],
      Map[MnId, Set[MeKey.Then.End]],
  )

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  private def spawn(bh: Behavior[Visualizer.Msg], name: String)(using tk: ActorTestKit): ActorRef[Visualizer.Msg] =
    tk.spawn(bh, s"test-visualizer-$name-${nameIdCounter.getAndIncrement()}")

  def apply(name: String)(using ActorTestKit, IORuntime): TestVisualizer = new TestVisualizer(
    api = Visualizer.spawn[IO](spawn).unsafeRunSync(),
  )

  extension (api: Visualizer)
    def ref: ActorRef[Visualizer.Msg] = api match
      case ApiImpl(ref) => ref

    def state(using ActorTestKit): State = getActorState[State](ref)

    // Allow access to the state from outside `mpi.actors.manager` package.
    def stateTyped(using testKit: ActorTestKit): VisualizerState = Tuple.fromProductTyped(state)
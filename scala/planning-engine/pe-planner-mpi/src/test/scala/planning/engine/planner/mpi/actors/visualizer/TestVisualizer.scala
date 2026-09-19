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

import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.visualizer.data.{Definition, State}
import planning.engine.planner.mpi.actors.visualizer.logic.{Actor, ApiImpl}

import java.util.concurrent.atomic.AtomicInteger

final case class TestVisualizer(api: Visualizer):
  import TestVisualizer.*

  def ref: ActorRef[Visualizer.Msg] = api.ref
  def state(using ActorTestKit, IORuntime): State = api.state
  def stateTyped(using ActorTestKit, IORuntime): VisualizerState = api.stateTyped

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

  

  def apply(viz: Visualization, name: String)(using tk: ActorTestKit, r: IORuntime): TestVisualizer =
    def spawn(bh: Behavior[Visualizer.Msg], name: String): ActorRef[Visualizer.Msg] =
      tk.spawn(bh, s"test-visualizer-$name-${nameIdCounter.getAndIncrement()}")
    
    new TestVisualizer(api = ApiImpl(Actor.spawn(Definition(viz), spawn), tk.system.scheduler))


  extension (api: Visualizer)
    def ref: ActorRef[Visualizer.Msg] = api match
      case ApiImpl(ref, _) => ref

    def state(using ActorTestKit, IORuntime): State = logObj("Visualizer", getActorState[State](ref))

    // Allow access to the state from outside `mpi.actors.manager` package.
    def stateTyped(using ActorTestKit, IORuntime): VisualizerState = Tuple.fromProductTyped(state)

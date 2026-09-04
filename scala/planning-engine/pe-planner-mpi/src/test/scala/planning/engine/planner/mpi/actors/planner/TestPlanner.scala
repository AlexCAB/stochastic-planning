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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.State
import planning.engine.planner.mpi.actors.planner.logic.ApiImpl
import planning.engine.planner.mpi.common.io.Variable

import java.util.concurrent.atomic.AtomicInteger

final case class TestPlanner(api: Planner):
  import TestPlanner.*

  def ref: ActorRef[Planner.Msg] = api.ref
  def state(using ActorTestKit, IORuntime): State = api.state
  def stateTyped(using ActorTestKit, IORuntime): PlannerState = api.stateTyped

object TestPlanner extends TestActorBase:
  type PlannerState = (
      Map[IoName, Map[IoIndex, Set[Node.Con]]],
      Map[MnId.Con, Node.Con],
  )

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  private def spawn(bh: Behavior[Planner.Msg], name: String)(using tk: ActorTestKit): ActorRef[Planner.Msg] =
    tk.spawn(bh, s"test-planner-$name-${nameIdCounter.getAndIncrement()}")

  def apply(
      name: String,
      inputVariables: Map[IoName, Variable.Input] = Map.empty,
      outputVariables: Map[IoName, Variable.Output] = Map.empty,
  )(using ActorTestKit, IORuntime): TestPlanner = new TestPlanner(
    api = Planner.spawn[IO](inputVariables, outputVariables, spawn).unsafeRunSync(),
  )

  extension (api: Planner)
    def ref: ActorRef[Planner.Msg] = api match
      case ApiImpl(ref) => ref

    def state(using ActorTestKit, IORuntime): State = logObj("Planner", getActorState[State](ref))

    // Allow access to the state from outside `mpi.actors.planner` package.
    def stateTyped(using ActorTestKit, IORuntime): PlannerState = Tuple.fromProductTyped(state)

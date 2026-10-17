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
import planning.engine.common.values.io.{IoTime, IoValue}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Message.TickDone
import planning.engine.planner.mpi.actors.planner.data.parts.ActiveSteps
import planning.engine.planner.mpi.actors.planner.data.{Definition, State}
import planning.engine.planner.mpi.actors.planner.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.model.io.{IoVars, Variable}

import java.util.concurrent.atomic.AtomicInteger

final case class TestPlanner(api: Planner):
  import TestPlanner.*

  def ref: ActorRef[Planner.Msg] = api.ref
  def getState(using ActorTestKit, IORuntime): State = api.getState
  def setState(newState: State)(using ActorTestKit, IORuntime): Unit = api.setState(newState)
  def stateTyped(using ActorTestKit, IORuntime): (PlannerStruct, PlannerTick) = api.stateTyped

object TestPlanner extends TestActorBase:
  type PlannerStruct = (
      Map[IoValue, Node.Con],
      Map[MnId.Con, Node.Con],
  )

  type PlannerTick = (
      IoTime,
      Option[ActorRef[TickDone]],
      Set[StepKey],
      Set[Node.Con],
      ActiveSteps,
      Set[StepKey],
      Set[StepKey],
  )

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  def apply(
      name: String,
      inVars: Set[Variable.Input] = Set.empty,
      outVars: Set[Variable.Output] = Set.empty,
  )(using tk: ActorTestKit, r: IORuntime): TestPlanner =
    def spawn(bh: Behavior[Planner.Msg], name: String): ActorRef[Planner.Msg] =
      tk.spawn(bh, s"test-planner-$name-${nameIdCounter.getAndIncrement()}")

    val definition = Definition(IoVars[IO](inVars, outVars).unsafeRunSync())
    new TestPlanner(api = ApiImpl(Actor.spawn(definition, spawn)))

  extension (api: Planner)
    def ref: ActorRef[Planner.Msg] = api match
      case ApiImpl(ref) => ref

    def getState(using ActorTestKit, IORuntime): State =
      val state = getActorState[State](ref)
      logObj("Planner struct", state.struct)
      logObj("Planner tick", state.tick)
      state

    def setState(newState: State)(using ActorTestKit, IORuntime): Unit =
      setActorState(ref, newState)
      logObj("Planner new struct", newState.struct)
      logObj("Planner new tick", newState.tick)

    // Allow access to the state from outside `mpi.actors.planner` package.
    def stateTyped(using ActorTestKit, IORuntime): (PlannerStruct, PlannerTick) =
      val st = getState
      (Tuple.fromProductTyped(st.struct), Tuple.fromProductTyped(st.tick))

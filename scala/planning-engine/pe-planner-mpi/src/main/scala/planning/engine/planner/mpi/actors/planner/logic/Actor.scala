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

package planning.engine.planner.mpi.actors.planner.logic

import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.Stateful
import planning.engine.planner.mpi.actors.planner.data.*

private[planner] object Actor extends Stateful with SimpleSync with Structure:
  import Message.*, Stateful.{GetState, SetState}

  override type Def = Definition
  override type Msg = Message | GetState[St] | SetState[St]

  override protected type St = State

  val name = "map-planner-actor"

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: Tick          => doTick(msg, state)
    case msg: ConNodesAdded => doConNodesAdded(msg, state)
    case msg: ActiveSteps   => doActiveSteps(msg, state)
    case msg: NextSteps     => doNextSteps(msg, state)
    case msg: RootSteps     => doRootSteps(msg, state)
    case msg: GetState[St]  => doGetState(msg, state)
    case msg: SetState[St]  => doSetState(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    doLogAndRaiseFatal("Planner actor error", Some(msg), state, err, "Error on message processing")

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)

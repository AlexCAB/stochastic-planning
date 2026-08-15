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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic

import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.actors.visualizer.data.*

private[visualizer] object Actor extends ActorBase with Structure:
  import Message.*, ActorBase.GetState

  override type Def = Definition
  override type Msg = Message | GetState[St]

  override protected type St = State

  val name = "map-visualizer-actor"

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: ShowNodesAdded => doNodesAdded(msg, state)
    case msg: ShowEdgesAdded => doEdgesAdded(msg, state)
    case msg: GetState[St]   => doGetState(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    doIgnoreError(msg, state, err)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)

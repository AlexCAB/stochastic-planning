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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node

import cats.effect.Sync
import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.actors.node.logic.Structure

object NodeActor extends ActorBase with Definitions with States with Messages with Structure:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(s"map-node-actor-${d.id}")
  
  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddEdgeSrc => doAddEdgeSrc(msg, state)
    case msg: AddEdgeTrg => doAddEdgeTrg(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = msg match
    case msg: AddEdge => msg.replay(ManagerAdaptor.EdgeError(err, msg.ref.key)).as(state)

  def spawn(definitions: List[Def], make: (Behavior[Msg], String) => Ref): Map[Ref, Def] =
    definitions.map(d => make(apply(d, State.init), d.id.value.toString) -> d).toMap

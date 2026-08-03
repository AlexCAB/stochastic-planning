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
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.node.data.{Definitions, States}
import planning.engine.planner.mpi.actors.node.logic.Structure

object NodeActor extends ActorBase with Definitions with States with Messages with Structure:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(s"map-node-actor-${d.id}")
  
  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddEdgeSrc => doAddEdgeSrc(msg, state)
    case msg: AddEdgeTrg => doAddEdgeTrg(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using d: Def, c: Ctx): F[St] =
    d.actors.manager ! ManagerActor.NodeActorError(c.self, Some(msg), err)
    state.pure

  def spawn(definitions: List[Def], make: (Behavior[Msg], String) => Ref): Map[Ref, Def] =
    definitions.map(d => make(apply(d, State.init), d.id.value.toString) -> d).toMap

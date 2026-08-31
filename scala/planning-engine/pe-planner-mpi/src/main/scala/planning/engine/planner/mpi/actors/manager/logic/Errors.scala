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
| created: 02.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.effect.Sync
import planning.engine.planner.mpi.actors.manager.data.Message.NodeActorError

private[manager] trait Errors:
  self: Actor.type =>

  // Called when any error occurs in some NodeActor.
  private[manager] def doHandleNodeError[F[_]: S](msg: NodeActorError, state: St)(using Def, Ctx): F[St] =
    logAndRaiseFatal(s"NodeActorError received: ${msg.nodeRef}", msg.msg, state, msg.err, "Node actor error")

  // Called when any error occurs in the ManagerActor itself.
  private[manager] def doHandleManagerError[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    logAndRaiseFatal("Manager actor error", Some(msg), state, err, "Manager actor error")

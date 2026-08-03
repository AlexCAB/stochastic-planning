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
| created: 02-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.syntax.all.*
import cats.ApplicativeThrow
import cats.effect.Sync
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.common.error.FatalException
import planning.engine.planner.mpi.common.repr.Representable

trait HandleError:
  self: ManagerActor.type =>

  private def renderOp[F[_]: S](prefix: String, obj: Option[Representable]): F[Option[String]] = obj
    .map(_.longAutoRepr.map(r => Some(prefix + "\n" + r.map(s => "    " + s.toString).mkString("\n"))))
    .getOrElse(None.pure)

  private def renderAtMsg[F[_]: S](msg: Option[Representable]): F[Option[String]] =
    renderOp("During processing message:", msg)

  private def renderAtSt[F[_]: S](state: St): F[Option[String]] = renderOp("Manager state:", Some(state))

  private def buildLogMsg(prefix: String, msgStr: Option[String], stateStr: Option[String]): String =
    List(Some(prefix), msgStr, stateStr).flatten.mkString("\n")

  // Called when any error occurs in some NodeActor.
  private[manager] def doHandleNodeError[F[_]: S](msg: NodeActorError, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      msgStr <- renderAtMsg(msg.msg)
      stateStr <- renderAtSt(state)
      logMst = buildLogMsg(s"NodeActorError received from NodeActor: ${msg.nodeRef}", msgStr, stateStr)
      _ <- logError(logMst, msg.err)
      _ <- ApplicativeThrow[F].raiseError(FatalException("Node actor error", Some(msg.err)))
    yield state

  // Called when any error occurs in the ManagerActor itself.
  private[manager] def doHandleManagerError[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    for
      msgStr <- renderAtMsg(Some(msg))
      stateStr <- renderAtSt(state)
      logMst = buildLogMsg(s"ManagerActor error", msgStr, stateStr)
      _ <- logError(logMst, err)
      _ <- ApplicativeThrow[F].raiseError(FatalException("Manager actor error", Some(err)))
    yield state

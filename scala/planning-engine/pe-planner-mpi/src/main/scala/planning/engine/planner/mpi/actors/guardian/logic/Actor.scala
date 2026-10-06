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
| created: 2026-09-13 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian.logic

import planning.engine.planner.mpi.actors.Stateless
import planning.engine.planner.mpi.actors.guardian.data.{Definition, Message}

private[guardian] object Actor extends Stateless with Lifecycle:
  import Message.*

  override type Def = Definition
  override type Msg = Message

  val name = "map-guardian-actor"

  override protected def setup[F[_]: S]()(using d: Def, ctx: Ctx): F[Unit] = delay(ctx.setLoggerName(name))

  override protected def receive[F[_]: S](msg: Msg)(using Def, Ctx): F[Bhv] = msg match
    case msg: Initialize => doInitialize(msg)
    case msg: Reset      => doReset(msg)

  override protected def error[F[_]: S](msg: Msg, err: Throwable)(using Def, Ctx): F[Bhv] =
    doLogAndRaiseFatal("Guardian actor error", Some(msg), err, "Error on message processing")

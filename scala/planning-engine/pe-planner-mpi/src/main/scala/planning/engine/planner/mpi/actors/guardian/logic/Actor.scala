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
import planning.engine.planner.mpi.actors.guardian.data.Message

private[guardian] object Actor extends Stateless with Lifecycle:
  import Message.*

  override type Msg = Message
  val name = "map-guardian-actor"

  override protected def setup()(using ctx: Ctx): Unit = ctx.setLoggerName(name)

  override protected def receive[F[_]: S](msg: Msg)(using Ctx): F[Bhv] = msg match
    case msg: Initialize => doInitialize(msg)
    case msg: Reset      => doReset(msg)

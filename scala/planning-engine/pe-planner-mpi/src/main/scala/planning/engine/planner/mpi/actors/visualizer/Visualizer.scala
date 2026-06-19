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

package planning.engine.planner.mpi.actors.visualizer

import cats.effect.Sync
import cats.effect.std.Dispatcher
import planning.engine.planner.mpi.actors.ActorBase

object Visualizer extends ActorBase with Definitions with States with Messages:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-visualizer-actor"

  override protected def receive[F[_]: {S, D}](msg: Msg, state: St)(using Def, Ctx): F[St] = ???

  override protected def error[F[_]: {S, D}](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = ???

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
import cats.effect.std.Dispatcher
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase

object Node extends ActorBase with Definitions with States with Messages:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-node-actor"

  override protected def receive[F[_]: {S, D}](msg: Msg, state: St)(using Def, Ctx): F[St] = ???

  override protected def error[F[_]: {S, D}](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = ???

  def spawn[F[_]: {Sync, Dispatcher}](definition: Def, make: (Behavior[Msg], String) => Ref): F[Node.Ref] =
    delay(make(apply(definition, State.init), definition.id.value.toString))

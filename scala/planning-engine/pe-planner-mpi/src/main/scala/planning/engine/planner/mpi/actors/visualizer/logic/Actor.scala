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

import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.Stateless
import planning.engine.planner.mpi.actors.visualizer.data.*

private[visualizer] object Actor extends Stateless with Structure:
  import Message.*

  type Def = Definition
  override type Msg = Message

  val name = "map-visualizer-actor"

  override protected def setup[F[_]: S]()(using d: Def, ctx: Ctx): F[Unit] =
    for
      _ <- delay(ctx.setLoggerName(name))
      _ <- d.visualization.init(d.metadata, d.variables)
    yield ()

  override protected def receive[F[_]: S](msg: Msg)(using Def, Ctx): F[Bhv] = msg match
    case msg: ShowNodesAdded => doNodesAdded(msg)
    case msg: ShowEdgesAdded => doEdgesAdded(msg)

  override protected def error[F[_]: S](msg: Msg, err: Throwable)(using Def, Ctx): F[Bhv] =
    doLogAndRaiseFatal("Visualizer actor error", Some(msg), err, "Error on message processing")

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition), name)

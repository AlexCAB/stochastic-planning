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
| created: 05.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic.modules

import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import planning.engine.planner.mpi.actors.visualizer.data.Message
import planning.engine.planner.mpi.actors.visualizer.logic.Actor

private[visualizer] trait Structure:
  self: Actor.type =>
  import Message.*

  private[visualizer] def doInit[F[_]: S](using d: Def, c: Ctx): F[Unit] =
    for
      _ <- logInfo(s"[Init] send visualization init message, metadata: ${d.metadata}, variables: ${d.variables}")
      _ <- d.visualization.init(d.metadata, d.variables)
    yield ()

  private[visualizer] def doNodesAdded[F[_]: S](msg: ShowNodesAdded)(using d: Def, c: Ctx): F[Bhv] =
    for
      _ <- logMap("[NodesAdded] added nodes", msg.ids)
      _ <- d.visualization.nodesAdded(msg.ids)
    yield Behaviors.same

  private[visualizer] def doEdgesAdded[F[_]: S](msg: ShowEdgesAdded)(using d: Def, c: Ctx): F[Bhv] =
    for
      _ <- logSeq("[EdgesAdded] added edges", msg.keys)
      _ <- d.visualization.edgesAdded(msg.keys)
    yield Behaviors.same

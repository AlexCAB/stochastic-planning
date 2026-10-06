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

package planning.engine.planner.mpi.actors.visualizer.logic

import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import planning.engine.planner.mpi.actors.visualizer.data.Message

private[visualizer] trait Structure:
  self: Actor.type =>
  import Message.*

  private[visualizer] def doNodesAdded[F[_]: S](msg: ShowNodesAdded)(using d: Def, c: Ctx): F[Bhv] =
    for
      _ <- logMap("[NodesAdded] added nodes", msg.ids.view.mapValues(_.repr).toMap)
      _ <- d.visualization.nodesAdded(msg.ids)
    yield Behaviors.same

  private[visualizer] def doEdgesAdded[F[_]: S](msg: ShowEdgesAdded)(using d: Def, c: Ctx): F[Bhv] =
    for
      _ <- logSeq(s"[EdgesAdded] added edges", msg.keys)
      _ <- d.visualization.edgesAdded(msg.keys)
    yield Behaviors.same

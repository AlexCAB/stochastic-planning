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
import planning.engine.planner.mpi.actors.visualizer.data.Message

private[visualizer] trait Structure:
  self: Actor.type =>
  import Message.*

  private[visualizer] def doNodesAdded[F[_]: S](msg: ShowNodesAdded, state: St)(using Def, Ctx): F[St] =
    for
        _ <- logInfo("[Structure.Nodes.Added] added nodes", msg.ids.view.mapValues(_.repr).toMap)
    yield state.withNodesAdded(msg.ids)

  private[visualizer] def doEdgesAdded[F[_]: S](msg: ShowEdgesAdded, state: St)(using Def, Ctx): F[St] =
    for
        _ <- logInfo(s"[Structure.Edges.Added] added edges: ${msg.keys}")
    yield state.withEdgesAdded(msg.keys)

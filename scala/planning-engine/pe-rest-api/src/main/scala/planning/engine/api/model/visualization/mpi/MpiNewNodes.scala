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
| created: 2026-10-07 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.{HnId, MnId}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.common.errors.assertionError

final case class MpiNewNodes(
    conIds: Set[HnId],
    absIds: Set[HnId],
    ioEdges: Set[(IoName, Set[HnId])],
) extends VisualizationMsg

object MpiNewNodes:
  def apply[F[_]: MT](ids: Map[MnId, NodeData]): F[MpiNewNodes] =
    def split: F[(Map[HnId, IoName], Set[HnId])] = ids.foldM((Map[HnId, IoName](), Set[HnId]())):
      case ((con, abs), (id: MnId.Con, d: NodeData.Con)) => (con + (id.asHnId -> d.ioName), abs).pure
      case ((con, abs), (id: MnId.Abs, d: NodeData.Abs)) => (con, abs + id.asHnId).pure
      case (_, (id, d))                                  => s"Unexpected MnId type: $id with data: $d".assertionError

    for
      (conMap, absSet) <- split
      ioEdges = conMap.groupBy(_._2).view.mapValues(_.keySet).toSet
    yield new MpiNewNodes(conMap.keySet, absSet, ioEdges)

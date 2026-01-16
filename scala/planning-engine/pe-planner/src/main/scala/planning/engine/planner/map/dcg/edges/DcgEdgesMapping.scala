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
| created: 2026-01-12 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds

final case class DcgEdgesMapping(
    forward: Map[HnId, Set[HnId]],
    backward: Map[HnId, Set[HnId]]
):
  lazy val isEmpty: Boolean = forward.isEmpty && backward.isEmpty

  private[edges] def validateJoin(acc: Map[HnId, Set[HnId]], hnId: HnId, targets: Set[HnId]): Boolean =
    acc.contains(hnId) && acc(hnId).intersect(targets).isEmpty

  private[edges] def joinIds[F[_]: MonadThrow](
      oldIds: Map[HnId, Set[HnId]],
      newIds: Map[HnId, Set[HnId]]
  ): F[Map[HnId, Set[HnId]]] = newIds.foldRight(oldIds.pure):
    case ((hnId, targets), accF) => accF.flatMap:
        case acc if validateJoin(acc, hnId, targets) => acc.updated(hnId, acc(hnId) ++ targets).pure
        case acc if !acc.contains(hnId)              => (acc + (hnId -> targets)).pure
        case acc => s"Can't add duplicate links: $hnId -> ${acc(hnId).intersect(targets)}".assertionError

  def addAll[F[_]: MonadThrow](ends: Set[EndIds]): F[DcgEdgesMapping] =
    for
      forward <- joinIds(forward, ends.groupBy(_.src).view.mapValues(_.map(_.trg).toSet).toMap)
      backward <- joinIds(backward, ends.groupBy(_.trg).view.mapValues(_.map(_.src).toSet).toMap)
    yield DcgEdgesMapping(forward, backward)

object DcgEdgesMapping:
  val empty: DcgEdgesMapping = DcgEdgesMapping(Map.empty, Map.empty)

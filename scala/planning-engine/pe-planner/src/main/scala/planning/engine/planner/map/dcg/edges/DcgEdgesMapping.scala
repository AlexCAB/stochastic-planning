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
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds

final case class DcgEdgesMapping[F[_]: MonadThrow](
    forward: Map[HnId, Set[HnId]],
    backward: Map[HnId, Set[HnId]]
) extends Validation:

  lazy val isEmpty: Boolean = forward.isEmpty && backward.isEmpty
  lazy val allEnds: Set[EndIds] = forward.flatMap((srcId, trgIds) => trgIds.map(trgId => EndIds(srcId, trgId))).toSet
  lazy val allHnIds: Set[HnId] = forward.keySet ++ backward.keySet

  override lazy val validationName: String = "DcgEdgesMapping"

  override lazy val validationErrors: List[Throwable] = validations(
    forward.keySet.haveSameElems(backward.values.flatten.toSet, "Forward and Backward mappings keys/values mismatch"),
    backward.keySet.haveSameElems(forward.values.flatten.toSet, "Backward and Forward mappings keys/values mismatch")
  )

  private[edges] def validateJoin(acc: Map[HnId, Set[HnId]], hnId: HnId, targets: Set[HnId]): Boolean =
    acc.contains(hnId) && acc(hnId).intersect(targets).isEmpty

  private[edges] def joinIds(
      oldIds: Map[HnId, Set[HnId]],
      newIds: Map[HnId, Set[HnId]]
  ): F[Map[HnId, Set[HnId]]] = newIds.foldRight(oldIds.pure):
    case ((hnId, targets), accF) => accF.flatMap:
        case acc if validateJoin(acc, hnId, targets) => acc.updated(hnId, acc(hnId) ++ targets).pure
        case acc if !acc.contains(hnId)              => (acc + (hnId -> targets)).pure
        case acc => s"Can't add duplicate links: $hnId -> ${acc(hnId).intersect(targets)}".assertionError

  private[edges] def findEnds(idsMap: Map[HnId, Set[HnId]], hnIds: Set[HnId]): Set[EndIds] =
    hnIds.flatMap(hnId => idsMap.get(hnId).toSet.flatMap(_.map(trgId => EndIds(hnId, trgId))))

  def addAll(ends: Iterable[EndIds]): F[DcgEdgesMapping[F]] =
    for
      (fMap, bMap) <- DcgEdgesMapping.makeEdgesMap(ends).pure
      forward <- joinIds(forward, fMap)
      backward <- joinIds(backward, bMap)
    yield DcgEdgesMapping(forward, backward)

  def findForward(sourceHnIds: Set[HnId]): Set[EndIds]= findEnds(forward, sourceHnIds)

  def findBackward(targetHnIds: Set[HnId]): Set[EndIds] = findEnds(backward, targetHnIds).map(_.swap)

object DcgEdgesMapping:
  private[edges] def makeEdgesMap(ends: Iterable[EndIds]): (Map[HnId, Set[HnId]], Map[HnId, Set[HnId]]) = (
    ends.groupBy(_.src).view.mapValues(_.map(_.trg).toSet).toMap,
    ends.groupBy(_.trg).view.mapValues(_.map(_.src).toSet).toMap
  )

  def empty[F[_]: MonadThrow]: DcgEdgesMapping[F] = DcgEdgesMapping(Map.empty, Map.empty)

  def apply[F[_]: MonadThrow](ends: Iterable[EndIds]): DcgEdgesMapping[F] =
    val (fMap, bMap) = DcgEdgesMapping.makeEdgesMap(ends)
    DcgEdgesMapping(fMap, bMap)

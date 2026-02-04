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
import planning.engine.common.values.edges.Edge

final case class DcgEdgesMapping[F[_]: MonadThrow](
    forward: Map[HnId, Set[HnId]],
    backward: Map[HnId, Set[HnId]]
) extends Validation:

  lazy val isEmpty: Boolean = forward.isEmpty && backward.isEmpty
  lazy val allEnds: Set[Edge.Ends] = forward.flatMap((srcId, trgIds) => trgIds.map(trgId => Edge.Ends(srcId, trgId))).toSet
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

  private[edges] def findEnds(idsMap: Map[HnId, Set[HnId]], hnIds: Set[HnId]): Set[Edge.Ends] =
    hnIds.flatMap(hnId => idsMap.get(hnId).toSet.flatMap(_.map(trgId => Edge.Ends(hnId, trgId))))

  private[edges] def formatMap(map: Map[HnId, Set[HnId]]): String =
    map.map((k, v) => s"    ${k.vStr} -> ${v.map(_.vStr).mkString(", ")}").mkString("\n")

  def addAll(ends: Iterable[Edge.Ends]): F[DcgEdgesMapping[F]] =
    for
      (fMap, bMap) <- DcgEdgesMapping.makeEdgesMap(ends).pure
      forward <- joinIds(forward, fMap)
      backward <- joinIds(backward, bMap)
    yield DcgEdgesMapping(forward, backward)

  def findForward(sourceHnIds: Set[HnId]): Set[Edge.Ends] = findEnds(forward, sourceHnIds)

  def findBackward(targetHnIds: Set[HnId]): Set[Edge.Ends] = findEnds(backward, targetHnIds).map(_.swap)

  override lazy val toString =
    s"""DcgEdgesMapping(
       | forward:\n${formatMap(forward)}
       | backward:\n${formatMap(backward)}
       |)""".stripMargin

object DcgEdgesMapping:
  private[edges] def makeEdgesMap(ends: Iterable[Edge.Ends]): (Map[HnId, Set[HnId]], Map[HnId, Set[HnId]]) = (
    ends.groupBy(_.src).view.mapValues(_.map(_.trg).toSet).toMap,
    ends.groupBy(_.trg).view.mapValues(_.map(_.src).toSet).toMap
  )

  def empty[F[_]: MonadThrow]: DcgEdgesMapping[F] = DcgEdgesMapping(Map.empty, Map.empty)

  def apply[F[_]: MonadThrow](ends: Iterable[Edge.Ends]): DcgEdgesMapping[F] =
    val (fMap, bMap) = DcgEdgesMapping.makeEdgesMap(ends)
    DcgEdgesMapping(fMap, bMap)

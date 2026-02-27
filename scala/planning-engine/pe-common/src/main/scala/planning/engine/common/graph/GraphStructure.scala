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
| created: 2026-01-27 |||||||||||*/

package planning.engine.common.graph

import cats.MonadThrow
import cats.syntax.all.*

import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.edge.EdgeKey.{End, Link, Then}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.node.MnId.{Con, filterCon, filterAbs}
import planning.engine.common.errors.*

import scala.annotation.tailrec
import scala.reflect.ClassTag

// GraphStructure represents the structure of the graph, providing algorithms
// for tracing and analysis of graph structure.
final case class GraphStructure[F[_]: MonadThrow](
    keys: Set[EdgeKey],
    srcMap: Map[MnId, Set[End]],
    trgMap: Map[MnId, Set[End]]
):
  lazy val mnIds: Set[MnId] = srcMap.keySet ++ trgMap.keySet
  lazy val conMnId: Set[MnId.Con] = mnIds.filterCon
  lazy val absMnId: Set[MnId.Abs] = mnIds.filterAbs

  private[graph] def filterByEndType[E <: End: ClassTag](map: Map[MnId, Set[End]]): Map[MnId, Set[E]] =
    val ct = implicitly[ClassTag[E]].runtimeClass
    map.view.mapValues(_.filter(e => ct.isInstance(e)).map(_.asInstanceOf[E])).filter((_, es) => es.nonEmpty).toMap

  lazy val srcLinkMap: Map[MnId, Set[Link.End]] = filterByEndType[Link.End](srcMap)
  lazy val srcThenMap: Map[MnId, Set[Then.End]] = filterByEndType[Then.End](srcMap)
  lazy val trgLinkMap: Map[MnId, Set[Link.End]] = filterByEndType[Link.End](trgMap)
  lazy val trgThenMap: Map[MnId, Set[Then.End]] = filterByEndType[Then.End](trgMap)

  lazy val neighbours: Map[MnId, Set[MnId]] = (srcMap.keySet ++ trgMap.keySet)
    .map(id => id -> (srcMap.getOrElse(id, Set()).map(_.id) ++ trgMap.getOrElse(id, Set()).map(_.id)))
    .toMap

  private[graph] def findConnected(start: MnId, visited: Set[MnId]): Set[MnId] = neighbours(start)
    .foldLeft(visited + start)((acc, nId) => if acc.contains(nId) then acc else findConnected(nId, acc))

  lazy val isConnected: Boolean =
    if neighbours.isEmpty then true
    else findConnected(neighbours.keys.head, Set()) == neighbours.keySet

  def add(ends: Iterable[EdgeKey]): F[GraphStructure[F]] =
    for
        _ <- this.keys.assertContainsNoneOf(ends, "Can't add Edges that already exist")
    yield GraphStructure(this.keys ++ ends)

  def findNextEdges(hnIds: Set[MnId]): Set[(MnId, MnId)] =
    hnIds.flatMap(hnId => srcMap.get(hnId).toSet.flatMap(_.map(trgId => (hnId, trgId.id))))

  def findNextLinks(ids: Set[MnId]): Set[(MnId, Link.End)] =
    ids.flatMap(id => srcLinkMap.get(id).toSet.flatMap(_.map(trgId => (id, trgId))))

  def traceAbsForestLayers(conIds: Set[Con]): F[List[Set[Link]]] =
    @tailrec def trace(next: Set[MnId], visited: Set[(MnId, Link.End)], acc: List[Set[Link]]): F[List[Set[Link]]] =
      val forward = findNextLinks(next)
      val intersect = visited.intersect(forward)

      (forward, intersect) match
        case (_, int) if int.nonEmpty               => s"Cycle detected on: $int".assertionError
        case (frw, _) if !frw.forall(_._2.id.isAbs) => s"Found LINK pointed on concrete node $frw".assertionError
        case (frw, _) if frw.isEmpty                => acc.pure
          
        case (frw, _) =>
          val trgMnIds = frw.map(_._2.id)
          val layer = frw.map((src, trg) => trg.asSrcKey(src))

          trace(trgMnIds, visited ++ frw, layer +: acc)

    trace(conIds.map(_.asMnId), Set.empty, List.empty).map(_.reverse)

  lazy val linkRoots: Set[MnId] = srcLinkMap.keySet -- trgLinkMap.keySet
  lazy val thenRoots: Set[MnId] = srcThenMap.keySet -- trgThenMap.keySet

  def findForward(srcMnIds: Set[MnId]): Set[EdgeKey] =
    srcMnIds.flatMap(srcId => srcMap.getOrElse(srcId, Set()).map(_.asSrcKey(srcId)))

  def findBackward(trgHnIds: Set[MnId]): Set[EdgeKey] =
    trgHnIds.flatMap(trgId => trgMap.getOrElse(trgId, Set()).map(_.asTrgKey(trgId)))

//  def traceAbstractLayers(conHnId: Set[HnId]): List[Set[HnId]] =
//    @tailrec def traceLayers(currentHnIds: Set[HnId], acc: List[Set[HnId]]): List[Set[HnId]] =
//      if currentHnIds.isEmpty then acc.reverse
//      else
//        val nextHnIds = findNextEdges(currentHnIds).map(_._2)
//        traceLayers(nextHnIds, currentHnIds :: acc)
//
//    traceLayers(conHnId, Nil)
//

object GraphStructure:
  def empty[F[_]: MonadThrow]: GraphStructure[F] = GraphStructure(Set.empty, Map.empty, Map.empty)

  def apply[F[_]: MonadThrow](keys: Set[EdgeKey]): GraphStructure[F] = GraphStructure(
    keys = keys,
    srcMap = keys.groupBy(_.src).view.mapValues(_.map(_.trgEnd).toSet).toMap,
    trgMap = keys.groupBy(_.trg).view.mapValues(_.map(_.srcEnd).toSet).toMap
  )

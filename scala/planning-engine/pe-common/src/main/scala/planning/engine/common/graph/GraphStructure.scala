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
import planning.engine.common.values.edges.Edge
import planning.engine.common.values.edges.Edge.{End, Link, Then}
import planning.engine.common.values.node.{HnId, MnId}

import scala.annotation.tailrec
import scala.reflect.ClassTag

trait GraphStructure[F[_]: MonadThrow]:
  def conNodeSet: Set[MnId.Con]
  def absNodeSet: Set[MnId.Abs]
  def edgeSet: Set[Edge]
  
  lazy val allMnId: Set[MnId] = conNodeSet ++ absNodeSet
  lazy val allHnId: Set[HnId] = allMnId.map(_.asHnId)

  lazy val srcMap: Map[MnId, Set[End]] = edgeSet.groupBy(_.src).view.mapValues(_.map(_.trgEnd).toSet).toMap
  lazy val trgMap: Map[MnId, Set[End]] = edgeSet.groupBy(_.trg).view.mapValues(_.map(_.srcEnd).toSet).toMap

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

  def findNextEdges(hnIds: Set[MnId]): Set[(MnId, MnId)] =
    hnIds.flatMap(hnId => srcMap.get(hnId).toSet.flatMap(_.map(trgId => (hnId, trgId.id))))

  def traceFromNodes(conHnId: Set[MnId]): (Boolean, Set[MnId]) = // (isDag, tracedAbsHnIds)
    @tailrec def trace(conHnId: Set[MnId], visited: Set[(MnId, MnId)]): (Boolean, Set[MnId]) =
      findNextEdges(conHnId) match
        case frw if visited.intersect(frw).nonEmpty => (false, visited.map(_._2)) // cycle detected
        case frw if frw.isEmpty                     => (true, visited.map(_._2)) // no more edges
        case frw                                    => trace(frw.map(_._2), visited ++ frw)

    trace(conHnId, Set.empty)

  lazy val linkRoots: Set[MnId] = srcLinkMap.keySet -- trgLinkMap.keySet
  lazy val thenRoots: Set[MnId] = srcThenMap.keySet -- trgThenMap.keySet

//  def traceAbstractLayers(conHnId: Set[HnId]): List[Set[HnId]] =
//    @tailrec def traceLayers(currentHnIds: Set[HnId], acc: List[Set[HnId]]): List[Set[HnId]] =
//      if currentHnIds.isEmpty then acc.reverse
//      else
//        val nextHnIds = findNextEdges(currentHnIds).map(_._2)
//        traceLayers(nextHnIds, currentHnIds :: acc)
//
//    traceLayers(conHnId, Nil)
//

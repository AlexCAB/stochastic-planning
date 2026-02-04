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

import planning.engine.common.values.edges.{Edge, End}
import planning.engine.common.values.node.HnId

import scala.annotation.tailrec
import scala.reflect.ClassTag

abstract class EndsGraph(getEdges: => Set[Edge]):
  lazy val edgeSet: Set[Edge] = getEdges
  lazy val allEdgesHnId: Set[HnId] = edgeSet.flatMap(e => Set(e.src, e.trg))

  lazy val srcMap: Map[HnId, Set[End]] = edgeSet.groupBy(_.src).view.mapValues(_.map(_.trgEnd).toSet).toMap
  lazy val trgMap: Map[HnId, Set[End]] = edgeSet.groupBy(_.trg).view.mapValues(_.map(_.srcEnd).toSet).toMap

  private[graph] def filterByEndType[E <: End: ClassTag](map: Map[HnId, Set[End]]): Map[HnId, Set[E]] =
    val ct = implicitly[ClassTag[E]].runtimeClass
    map.view.mapValues(_.filter(e => ct.isInstance(e)).map(_.asInstanceOf[E])).filter((_, es) => es.nonEmpty).toMap

  lazy val srcLinkMap: Map[HnId, Set[End.Link]] = filterByEndType[End.Link](srcMap)
  lazy val srcThenMap: Map[HnId, Set[End.Then]] = filterByEndType[End.Then](srcMap)
  lazy val trgLinkMap: Map[HnId, Set[End.Link]] = filterByEndType[End.Link](trgMap)
  lazy val trgThenMap: Map[HnId, Set[End.Then]] = filterByEndType[End.Then](trgMap)

  lazy val neighbours: Map[HnId, Set[HnId]] = (srcMap.keySet ++ trgMap.keySet)
    .map(id => id -> (srcMap.getOrElse(id, Set()).map(_.id) ++ trgMap.getOrElse(id, Set()).map(_.id)))
    .toMap

  private[graph] def findConnected(start: HnId, visited: Set[HnId]): Set[HnId] = neighbours(start)
    .foldLeft(visited + start)((acc, nId) => if acc.contains(nId) then acc else findConnected(nId, acc))

  lazy val isConnected: Boolean =
    if neighbours.isEmpty then true
    else findConnected(neighbours.keys.head, Set()) == neighbours.keySet

  def findNextEdges(hnIds: Set[HnId]): Set[(HnId, HnId)] =
    hnIds.flatMap(hnId => srcMap.get(hnId).toSet.flatMap(_.map(trgId => (hnId, trgId.id))))

  def traceFromNodes(conHnId: Set[HnId]): (Boolean, Set[HnId]) = // (isDag, tracedAbsHnIds)
    @tailrec def trace(conHnId: Set[HnId], visited: Set[(HnId, HnId)]): (Boolean, Set[HnId]) =
      findNextEdges(conHnId) match
        case frw if visited.intersect(frw).nonEmpty => (false, visited.map(_._2)) // cycle detected
        case frw if frw.isEmpty                     => (true, visited.map(_._2)) // no more edges
        case frw                                    => trace(frw.map(_._2), visited ++ frw)

    trace(conHnId, Set.empty)

  lazy val linkRoots: Set[HnId] = srcLinkMap.keySet -- trgLinkMap.keySet
  lazy val thenRoots: Set[HnId] = srcThenMap.keySet -- trgThenMap.keySet

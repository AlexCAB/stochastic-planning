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

import planning.engine.common.values.edges.EndIds
import planning.engine.common.values.node.HnId

import scala.annotation.tailrec

abstract class EndsGraph(getEnds: => Set[EndIds]):
  
  private[graph] def findConnected(start: HnId, visited: Set[HnId]): Set[HnId] = neighbours(start)
    .foldLeft(visited + start)((acc, nId) => if acc.contains(nId) then acc else findConnected(nId, acc))

  lazy val ends = getEnds

  lazy val srcMap: Map[HnId, Set[HnId]] = ends.groupBy(_.src).view.mapValues(_.map(_.trg).toSet).toMap
  lazy val trgMap: Map[HnId, Set[HnId]] = ends.groupBy(_.trg).view.mapValues(_.map(_.src).toSet).toMap

  lazy val neighbours: Map[HnId, Set[HnId]] = (srcMap.keySet ++ trgMap.keySet)
    .map(id => id -> (srcMap.getOrElse(id, Set()) ++ trgMap.getOrElse(id, Set())))
    .toMap

  lazy val isConnected: Boolean =
    if neighbours.isEmpty then true
    else findConnected(neighbours.keys.head, Set()) == neighbours.keySet

  def findNextEdges(hnIds: Set[HnId]): Set[(HnId, HnId)] =
    hnIds.flatMap(hnId => srcMap.get(hnId).toSet.flatMap(_.map(trgId => (hnId, trgId))))

  def traceFromNodes(conHnId: Set[HnId]): (Boolean, Set[HnId]) = // (isDag, tracedAbsHnIds)
    @tailrec def trace(conHnId: Set[HnId], visited: Set[(HnId, HnId)]): (Boolean, Set[HnId]) =
      findNextEdges(conHnId) match
        case frw if visited.intersect(frw).nonEmpty => (false, visited.map(_._2)) // cycle detected
        case frw if frw.isEmpty                     => (true, visited.map(_._2)) // no more edges
        case frw                                    => trace(frw.map(_._2), visited ++ frw)

    trace(conHnId, Set.empty)

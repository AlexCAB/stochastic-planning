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

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{End, Link, Then}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.node.MnId.{filterAbs, filterCon}

import scala.reflect.ClassTag

// GraphStructure represents the structure of the graph, providing algorithms
// for tracing and analysis of graph structure.
final case class GraphStructure(
    keys: Set[MeKey],
    srcMap: Map[MnId, Set[End]],
    trgMap: Map[MnId, Set[End]],
) extends GraphTracing:
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

  lazy val linkRoots: Set[MnId] = srcLinkMap.keySet -- trgLinkMap.keySet
  lazy val thenRoots: Set[MnId] = srcThenMap.keySet -- trgThenMap.keySet

  lazy val neighbours: Map[MnId, Set[MnId]] = (srcMap.keySet ++ trgMap.keySet)
    .map(id => id -> (srcMap.getOrElse(id, Set()).map(_.id) ++ trgMap.getOrElse(id, Set()).map(_.id)))
    .toMap

  private[graph] def findConnected(start: MnId, visited: Set[MnId]): Set[MnId] = neighbours(start)
    .foldLeft(visited + start)((acc, nId) => if acc.contains(nId) then acc else findConnected(nId, acc))

  lazy val isConnected: Boolean =
    if neighbours.isEmpty then true
    else findConnected(neighbours.keys.head, Set()) == neighbours.keySet

  def add[F[_]: MT](ends: Iterable[MeKey]): F[GraphStructure] =
    for
        _ <- this.keys.assertContainsNoneOf(ends, "Can't add Edges that already exist")
    yield GraphStructure(this.keys ++ ends)

object GraphStructure:
  def empty: GraphStructure = GraphStructure(Set.empty, Map.empty, Map.empty)

  def apply(keys: Set[MeKey]): GraphStructure = GraphStructure(
    keys = keys,
    srcMap = keys.groupBy(_.src).view.mapValues(_.map(_.trgEnd).toSet).toMap,
    trgMap = keys.groupBy(_.trg).view.mapValues(_.map(_.srcEnd).toSet).toMap,
  )

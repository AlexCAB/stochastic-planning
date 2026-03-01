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
| created: 2026-03-01 |||||||||||*/

package planning.engine.common.graph

import cats.MonadThrow
import cats.syntax.all.*

import planning.engine.common.graph.edges.EdgeKey.{Link, Then}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.node.MnId.Con
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.EdgeKey
import planning.engine.common.graph.paths.Path

import scala.annotation.tailrec

// Set of algorithms for tracing of graph structure,
// e.g. for finding paths between nodes, finding cycles, etc.
trait GraphTracing[F[_]: MonadThrow]:
  self: GraphStructure[F] =>

  private[graph] def findNextLinks[E <: EdgeKey.End](srcIds: Set[MnId], edgeMap: Map[MnId, Set[E]]): Set[(MnId, E)] =
    srcIds.flatMap(id => edgeMap.get(id).toSet.flatMap(_.map(trgId => (id, trgId))))

  def traceAbsForestLayers(conIds: Set[Con]): F[List[Set[Link]]] =
    @tailrec def trace(next: Set[MnId], visited: Set[(MnId, Link.End)], acc: List[Set[Link]]): F[List[Set[Link]]] =
      val forward = findNextLinks(next, srcLinkMap)
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

  def traceThenPaths(beginHnIds: Set[MnId]): F[(Set[Path], Set[MnId])] =
    def reduce(res: List[(Set[Path], Set[MnId])]): (Set[Path], Set[MnId]) =
      res.reduce((a, b) => (a._1 ++ b._1, a._2 ++ b._2))

    def trace(cur: MnId, vis: Set[MnId], acc: Vector[(MnId, Then.End)]): F[(Set[Path], Set[MnId])] =
      srcThenMap.get(cur) match
        case None if acc.isEmpty => (Set(), vis + cur).pure // Single node with no outgoing THEN edges
        case None                => Path.Direct(acc).map(ps => (Set(ps), vis + cur))

        case Some(ends) if vis.contains(cur) && acc.headOption.exists(_._1 == cur) =>
          Path.Loop(acc).map(ps => (Set(ps), vis)) // Is Loop if current node is visited and is start of the path

        case Some(ends) if vis.contains(cur) => Path.Noose(acc).map(ps => (Set(ps), vis))

        case Some(ends) => ends.toList
            .traverse(end => trace(end.id, vis + cur, acc :+ (cur, end)))
            .map(reduce)

    beginHnIds.toList
      .traverse(id => trace(id, Set.empty, Vector.empty))
      .map(reduce)

  private[graph] def traceThenCyclesPaths(visited: Set[MnId], acc: Set[Path]): F[Set[Path]] =
    val notVisited = mnIds -- visited

    println("Visited: " + visited + " | Not visited: " + notVisited + " | Acc: " + acc)

    if notVisited.isEmpty then acc.pure
    else
      traceThenPaths(Set(notVisited.minBy(_.value)))
        .flatMap((paths, vis) => traceThenCyclesPaths(visited ++ vis, acc ++ paths))

  lazy val allThenPaths: F[Set[Path]] =
    for
      (rootedPaths, visited) <- traceThenPaths(thenRoots)
      cyclesPaths <- traceThenCyclesPaths(visited, Set.empty)
    yield rootedPaths ++ cyclesPaths

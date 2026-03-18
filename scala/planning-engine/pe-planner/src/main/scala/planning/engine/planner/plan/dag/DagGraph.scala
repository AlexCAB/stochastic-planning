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
| created: 2026-03-12 |||||||||||*/

package planning.engine.planner.plan.dag

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.node.PnId
import planning.engine.planner.plan.dag.edges.DagEdge
import planning.engine.planner.plan.dag.nodes.DagNode
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.PeKey
import planning.engine.planner.plan.repr.DagGraphRepr

// Planning DAG, here algorithms for building and tracing (similar to GraphStructure[F])
// It represent whole general plan-graph structure, so context node and plan node are together in this graph.
final case class DagGraph[F[_]: MonadThrow](
    nodes: Map[PnId, DagNode[F]],
    edges: Map[PeKey, DagEdge[F]]
) extends DagGraphRepr[F]:

  private[dag] lazy val linkEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isLink)
  private[dag] lazy val thenEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isThen)

  private[dag] def makeSrcMap(edges: Iterable[DagEdge[F]]): Map[PnId, Set[PnId]] =
    edges.groupBy(_.key.src).map((k, vs) => k -> vs.map(_.key.trg).toSet)

  private[dag] def makeTrgLinkMap(edges: Iterable[DagEdge[F]]): Map[PnId, Set[PnId]] =
    edges.groupBy(_.key.trg).map((k, vs) => k -> vs.map(_.key.src).toSet)

  private[dag] def makeTrgThenMap(edges: Iterable[DagEdge[F]]): F[Map[PnId, PnId]] =
    edges.groupBy(_.key.trg).foldLeft(Map[PnId, PnId]().pure):
      case (acc, (trg, vs)) if vs.size == 1 => acc.map(_ + (trg -> vs.head.key.src))
      case (acc, (trg, vs)) => s"Planning DAG need to be a forest, found joint link at trg: $trg".assertionError

  lazy val srcLinkMap: Map[PnId, Set[PnId]] = makeSrcMap(linkEdges)
  lazy val trgLinkMap: Map[PnId, Set[PnId]] = makeTrgLinkMap(linkEdges)

  lazy val srcThenMap: Map[PnId, Set[PnId]] = makeSrcMap(thenEdges)
  lazy val trgThenMap: F[Map[PnId, PnId]] = makeTrgThenMap(thenEdges)

//  def traceAbsDagLayers(conIds: Set[Con], filterForward: Link => Boolean): F[List[Set[Link]]] =
//    @tailrec def trace(next: Set[MnId], visited: Set[(MnId, Link.End)], acc: List[Set[Link]]): F[List[Set[Link]]] =
//      val forward = findInEdgeMap(next, srcLinkMap).filter((src, end) => filterForward(end.asSrcKey(src)))
//      val intersect = visited.intersect(forward)
//
//      (forward, intersect) match
//        case (_, int) if int.nonEmpty => s"Cycle detected on: $int".assertionError
//        case (frw, _) if !frw.forall(_._2.id.isAbs) => s"Found LINK pointed on concrete node $frw".assertionError
//        case (frw, _) if frw.isEmpty => acc.pure
//
//        case (frw, _) =>
//          val trgMnIds = frw.map(_._2.id)
//          val layer = frw.map((src, trg) => trg.asSrcKey(src))
//
//          trace(trgMnIds, visited ++ frw, layer +: acc)
//
//    trace(conIds.map(_.asMnId), Set.empty, List.empty).map(_.reverse)

object DagGraph:
  def empty[F[_]: MonadThrow]: DagGraph[F] = DagGraph(Map.empty, Map.empty)

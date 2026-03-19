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
import planning.engine.common.values.node.PnId.Con
import planning.engine.planner.plan.dag.edges.DagEdge
import planning.engine.planner.plan.dag.nodes.DagNode
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.PeKey
import planning.engine.common.graph.edges.PeKey.{Link, Then}
import planning.engine.planner.plan.repr.DagGraphRepr

import scala.annotation.tailrec

// Planning DAG, here algorithms for building and tracing (similar to GraphStructure[F])
// It represent whole general plan-graph structure, so context node and plan node are together in this graph.
final case class DagGraph[F[_]: MonadThrow](
    nodes: Map[PnId, DagNode[F]],
    edges: Map[PeKey, DagEdge[F]]
) extends DagGraphRepr[F]:

  private[dag] lazy val linkEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isLink)
  private[dag] lazy val thenEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isThen)

  private[dag] def makeSrcMap[K <: PeKey](edges: Iterable[DagEdge[F]]): Map[PnId, Set[K]] =
    edges.groupBy(_.key.src).map((k, vs) => k -> vs.map(_.key.asInstanceOf[K]).toSet)

  private[dag] def makeTrgLinkMap(edges: Iterable[DagEdge[F]]): Map[PnId, Set[Link]] =
    edges.groupBy(_.key.trg).map((k, vs) => k -> vs.map(_.key.asInstanceOf[Link]).toSet)

  private[dag] def makeTrgThenMap(edges: Iterable[DagEdge[F]]): F[Map[PnId, Then]] =
    edges.groupBy(_.key.trg).foldLeft(Map[PnId, Then]().pure):
      case (acc, (trg, vs)) if vs.size == 1 && vs.head.isThen => acc.map(_ + (trg -> vs.head.key.asInstanceOf[Then]))
      case (acc, (trg, vs)) if vs.size == 1 => acc // If it's not THEN edge, we can ignore it for THEN map
      case (acc, (trg, vs)) => s"Planning DAG need to be a forest, found joint link at trg: $trg".assertionError

  lazy val srcLinkMap: Map[PnId, Set[Link]] = makeSrcMap[Link](linkEdges)
  lazy val trgLinkMap: Map[PnId, Set[Link]] = makeTrgLinkMap(linkEdges)

  lazy val srcThenMap: Map[PnId, Set[Then]] = makeSrcMap[Then](thenEdges)
  lazy val trgThenMap: F[Map[PnId, Then]] = makeTrgThenMap(thenEdges)

  private[dag] def findInEdgeMap[K <: PeKey](ids: Set[PnId], edgeMap: Map[PnId, Set[K]]): Set[K] =
    ids.flatMap(id => edgeMap.get(id).toSet.flatten)

  def traceAbsDagLayers(conIds: Set[Con]): F[List[Set[Link]]] =
    @tailrec def trace(next: Set[PnId], visited: Set[Link], acc: List[Set[Link]]): F[List[Set[Link]]] =
      val forward = findInEdgeMap(next, srcLinkMap)
      val intersect = visited.intersect(forward)

      if intersect.nonEmpty then s"Cycle detected on: $intersect".assertionError
      else if !forward.forall(_.trg.mnId.isAbs) then s"Found LINK pointed on concrete node $forward".assertionError
      else if forward.isEmpty then acc.pure
      else trace(forward.map(_.trg), visited ++ forward, forward +: acc)

    trace(conIds.map(_.asPnId), Set.empty, List.empty).map(_.reverse)

object DagGraph:
  def empty[F[_]: MonadThrow]: DagGraph[F] = DagGraph(Map.empty, Map.empty)

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
import planning.engine.common.values.node.SnId
import planning.engine.planner.plan.dag.edges.DagEdge
import planning.engine.planner.plan.dag.nodes.DagNode
import planning.engine.common.errors.*
import planning.engine.planner.plan.repr.DagGraphRepr

// Planning DAG, here algorithms for building and tracing (similar to GraphStructure[F])
// It represent whole general plan-graph structure, so context node and plan node are together in this graph.
final case class DagGraph[F[_]: MonadThrow](
    nodes: Map[SnId, DagNode[F]],
    edges: Map[DagEdge.Key, DagEdge[F]]
) extends DagGraphRepr[F]:
  private[dag] lazy val linkEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isLink)
  private[dag] lazy val thenEdges: Iterable[DagEdge[F]] = edges.values.filter(_.isThen)

  private[dag] def makeSrcMap(edges: Iterable[DagEdge[F]]): Map[SnId, Set[SnId]] =
    edges.groupBy(_.key.src).map((k, vs) => k -> vs.map(_.key.trg).toSet)

  private[dag] def makeTrgLinkMap(edges: Iterable[DagEdge[F]]): Map[SnId, Set[SnId]] =
    edges.groupBy(_.key.trg).map((k, vs) => k -> vs.map(_.key.src).toSet)

  private[dag] def makeTrgThenMap(edges: Iterable[DagEdge[F]]): F[Map[SnId, SnId]] =
    edges.groupBy(_.key.trg).foldLeft(Map[SnId, SnId]().pure):
      case (acc, (trg, vs)) if vs.size == 1 => acc.map(_ + (trg -> vs.head.key.src))
      case (acc, (trg, vs)) => s"Planning DAG need to be a forest, found joint link at trg: $trg".assertionError

  lazy val srcLinkMap: Map[SnId, Set[SnId]] = makeSrcMap(linkEdges)
  lazy val trgLinkMap: Map[SnId, Set[SnId]] = makeTrgLinkMap(linkEdges)

  lazy val srcThenMap: Map[SnId, Set[SnId]] = makeSrcMap(thenEdges)
  lazy val trgThenMap: F[Map[SnId, SnId]] = makeTrgThenMap(thenEdges)

object DagGraph:
  def empty[F[_]: MonadThrow]: DagGraph[F] = DagGraph(Map.empty, Map.empty)

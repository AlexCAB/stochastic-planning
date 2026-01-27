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
| created: 2026-01-11 |||||||||||*/

package planning.engine.planner.map.dcg

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.validation.Validation
import planning.engine.common.errors.*
import planning.engine.common.graph.EndsGraph
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.common.values.edges.EndIds
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.map.samples.sample.SampleData

// Is DAG where leafs are concrete hidden nodes and rest of the tree is abstract hidden nodes.
// Edges with type LINK pointed form higher abstract root nodes to concrete leaf nodes.
// Also include THEN edges to previous nodes.
final case class ActiveAbsDag[F[_]: MonadThrow](
    backwordThenEnds: Set[EndIds], // Targets of THEN edges is nodes in this graph
    graph: DcgGraph[F]
) extends EndsGraph(graph.edgesData.keySet) with Validation:
  override lazy val validationName: String = "ActiveAbsGraph"

  override lazy val validationErrors: List[Throwable] =
    val allConHnIds = graph.concreteNodes.keySet
    val allAbsHnIds = graph.abstractNodes.keySet
    val allBackThenIds = backwordThenEnds.flatMap(e => Set(e.src, e.trg))
    val (isDag, tracedAbsHnIds) = traceFromNodes(allConHnIds)

    graph.validationErrors ++ validations(
      graph.allHnIds.containsAllOf(allBackThenIds, "Back THEN edges refer to unknown HnIds"),
      isDag -> "Graph contains cycles in LINK edges",
      tracedAbsHnIds.haveSameElems(allAbsHnIds, "Some nodes are not connected to any concrete nodes via LINK edges")
    )

  def addAbstractLevel(
      abstractNodes: Iterable[DcgNode.Abstract[F]],
      forwardLinkEdges: Iterable[DcgEdgeData],
      backThenEnds: Set[EndIds]
  ): F[ActiveAbsDag[F]] =
    for
      withNodes <- graph.addAbsNodes(abstractNodes)
      withEdges <- withNodes.addEdges(forwardLinkEdges)
      _ <- backwordThenEnds.assertNoSameElems(backThenEnds, "Graph structure bug: duplicate THEN edges detected")
      backTrgIds = backThenEnds.map(_.trg)
      _ <- withNodes.allHnIds.assertContainsAll(backTrgIds, "Back then target refers to unknown HnIds")
    yield this.copy(
      backwordThenEnds = backwordThenEnds ++ backThenEnds,
      graph = withEdges
    )

object ActiveAbsDag:
  def apply[F[_]: MonadThrow](
      concreteNodes: Iterable[DcgNode.Concrete[F]],
      abstractNodes: Iterable[DcgNode.Abstract[F]],
      forwardLinkEdges: Iterable[DcgEdgeData],
      backThenEnds: Set[EndIds],
      samples: Iterable[SampleData]
  ): F[ActiveAbsDag[F]] =
    for
      graph <- DcgGraph(concreteNodes, abstractNodes, forwardLinkEdges, samples)
      backTrgIds = backThenEnds.map(_.trg)
      _ <- graph.allHnIds.assertContainsAll(backTrgIds, "Back then target refers to unknown HnIds")
    yield new ActiveAbsDag(backwordThenEnds = backThenEnds, graph = graph)

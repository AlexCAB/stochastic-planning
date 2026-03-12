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

package planning.engine.planner.map.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.graph.GraphTracing.allLinksFilter
import planning.engine.common.graph.edges.{EdgeKey, EdgeKeySet}
import planning.engine.common.values.node.MnId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.repr.ActiveAbsDagRepr

// Is DAG where leafs are concrete hidden nodes and rest of the tree is abstract hidden nodes.
// Edges with type LINK pointed form higher abstract root nodes to concrete leaf nodes.
// Also include THEN edges to previous nodes.
final case class ActiveAbsDag[F[_]: MonadThrow](
    backwordKeys: EdgeKeySet[EdgeKey.Then], // Targets of THEN edges is nodes in this graph
    graph: DcgGraph[F]
) extends ActiveAbsDagRepr[F]

object ActiveAbsDag:
  def apply[F[_]: MonadThrow](
      nodes: Iterable[DcgNode[F]],
      linkEdges: Iterable[DcgEdge[F]],
      backThenKeys: Set[EdgeKey.Then],
      samples: Iterable[SampleData]
  ): F[ActiveAbsDag[F]] =
    for
      _ <- linkEdges.forall(_.key.isInstanceOf[EdgeKey.Link]).assertTrue("Only LINK edges can be added")
      graph <- DcgGraph(nodes, linkEdges, samples)
      backwordKeys = EdgeKeySet[EdgeKey.Then](backThenKeys)
      _ <- graph.mnIds.assertContainsAllOf(backwordKeys.trgIds, "Back THEN target edges refer to unknown HnIds")
      _ <- graph.edgesMdIds.assertContainsAllOf(backwordKeys.trgIds, "Target do not connected to active graph")
      tracedAbs <- graph.structure.traceAbsDagLayers(graph.conMnId, allLinksFilter).map(_.flatten.map(_.trg))
      _ <- tracedAbs.assertSameElems(graph.absMnId, "Some nodes are not connected to any concrete nodes")
    yield new ActiveAbsDag(backwordKeys, graph)

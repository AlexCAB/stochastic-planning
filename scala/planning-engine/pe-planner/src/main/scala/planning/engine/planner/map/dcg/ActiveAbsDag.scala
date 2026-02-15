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
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.dcg.DcgGraph

import planning.engine.common.values.edge.EdgeKey

// Is DAG where leafs are concrete hidden nodes and rest of the tree is abstract hidden nodes.
// Edges with type LINK pointed form higher abstract root nodes to concrete leaf nodes.
// Also include THEN edges to previous nodes.
final case class ActiveAbsDag[F[_]: MonadThrow](
    backwordThenKeys: Set[EdgeKey], // Targets of THEN edges is nodes in this graph
    graph: DcgGraph[F]
) extends Validation:

  override lazy val validations: (String, List[Throwable]) =
    val allBackThenIds = backwordThenKeys.flatMap(e => Set(e.src, e.trg))
    val (isDag, tracedAbsHnIds) = graph.structure.traceFromNodes(graph.conMnId.map(_.asInstanceOf[MnId]))

    validate("ActiveAbsGraph", graph.validations)(
      graph.mnIds.containsAllOf(allBackThenIds, "Back THEN edges refer to unknown HnIds"),
      isDag -> "Graph contains cycles in LINK edges",
      tracedAbsHnIds.haveSameElems(graph.absMnId, "Some nodes are not connected to any concrete nodes via LINK edges")
    )

// TODO Refactoring:

//  def addAbstractLevel(
//      nodes: Iterable[DcgNode.Abstract[F]],
//      linkEdges: Iterable[DcgEdge[F]],
//      backThenEnds: Set[EdgeKey]
//  ): F[ActiveAbsDag[F]] =
//    for
//      _ <- linkEdges.forall(_.key.isInstanceOf[EdgeKey.Link]).assertTrue("Only LINK edges can be added")
//      withNodes <- graph.addNodes(nodes)
//      withEdges <- withNodes.addEdges(linkEdges)
//      _ <- backwordThenKeys.assertNoSameElems(backThenEnds, "Graph structure bug: duplicate THEN edges detected")
//      backTrgIds = backThenEnds.map(_.trg)
//      _ <- withNodes.mnIds.assertContainsAll(backTrgIds, "Back then target refers to unknown HnIds")
//    yield this.copy(
//      backwordThenKeys = backwordThenKeys ++ backThenEnds,
//      graph = withEdges
//    )
//
//object ActiveAbsDag:
//  def apply[F[_]: MonadThrow](
//      nodes: Iterable[DcgNode[F]],
//      linkEdges: Iterable[DcgEdge[F]],
//      backThenEnds: Set[EdgeKey],
//      samples: Iterable[SampleData]
//  ): F[ActiveAbsDag[F]] =
//    for
//      _ <- linkEdges.forall(_.key.isInstanceOf[EdgeKey.Link]).assertTrue("Only LINK edges can be added")
//      graph <- DcgGraph(nodes, linkEdges, samples)
//      backTrgIds = backThenEnds.map(_.trg)
//      _ <- graph.mnIds.assertContainsAll(backTrgIds, "Back then target refers to unknown HnIds")
//    yield new ActiveAbsDag(backwordThenEnds = backThenEnds, graph = graph)

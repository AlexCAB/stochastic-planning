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
| created: 2025-12-10 |||||||||||*/

package planning.engine.planner.map.state

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.common.errors.*
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.nodes.DcgNode

final case class MapGraphState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[HnId]],
    graph: DcgGraph[F]
):
  lazy val isEmpty: Boolean = ioValues.isEmpty && graph.isEmpty
  
  def addConcreteNodes(nodes: List[DcgNode.Concrete[F]]): F[MapGraphState[F]] =
    for
      groupedIoVals <- nodes.groupBy(_.ioValue).view.mapValues(_.map(_.id).toSet).pure
      _ <- ioValues.keySet.assertNoSameElems(groupedIoVals.keySet, "Can't add IoValues that already exist")
      newGraph <- graph.addConNodes(nodes)
    yield this.copy(
      ioValues = ioValues ++ groupedIoVals,
      graph = newGraph
    )

  def addAbstractNodes(nodes: List[DcgNode.Abstract[F]]): F[MapGraphState[F]] =
    for
      newGraph <- graph.addAbsNodes(nodes)
    yield this.copy(graph = newGraph)

  def addEdges(newEdges: List[DcgEdgeData]): F[MapGraphState[F]] =
    for
      newGraph <- graph.addEdges(newEdges)
    yield this.copy(graph = newGraph)

  def mergeEdges(newEdges: List[DcgEdgeData]): F[MapGraphState[F]] =
    for
      newGraph <- graph.mergeEdges(newEdges)
    yield this.copy(graph = newGraph)

  def addSamples(samples: List[SampleData]): F[MapGraphState[F]] =
    for
      newGraph <- graph.addSamples(samples)
    yield this.copy(graph = newGraph)

  def findConForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList
        .traverse(v => ioValues(v).toList.traverse(id => graph.getConForHnId(id)).map(n => v -> n.toSet))
    yield (foundNodes.toMap, notFoundValues)

  override def toString: String =
    s"DcgState(concreteNodes = ${graph.concreteNodes.keySet}, abstractNodes = ${graph.abstractNodes.keySet})"

object MapGraphState:
  def empty[F[_]: MonadThrow]: MapGraphState[F] = new MapGraphState[F](
    ioValues = Map.empty,
    graph = DcgGraph.empty[F]
  )

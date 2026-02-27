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
import planning.engine.common.graph.io.IoValueMap
import planning.engine.common.values.node.MnId
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.samples.DcgSample

final case class MapGraphState[F[_]: MonadThrow](
    ioValues: IoValueMap[F],
    graph: DcgGraph[F]
):
  lazy val isEmpty: Boolean = ioValues.isEmpty && graph.isEmpty
  lazy val ioValuesMnId: Set[MnId.Con] = ioValues.values.flatten.toSet

  def addNodes(nodes: Iterable[DcgNode[F]]): F[MapGraphState[F]] =
    for
      conNodes <- nodes.filter(_.isInstanceOf[DcgNode.Concrete[F]]).map(_.asInstanceOf[DcgNode.Concrete[F]]).pure
      _ <- ioValuesMnId.assertContainsNoneOf(conNodes.map(_.id).toSet, "Two or more nodes can't have the same MnId")
      newIoValues <- ioValues.addIoValues(conNodes.map(n => n.ioValue -> n.id))
      newGraph <- graph.addNodes(nodes)
    yield this.copy(
      ioValues = newIoValues,
      graph = newGraph
    )

  def addSamples(samples: Iterable[DcgSample.Add[F]]): F[MapGraphState[F]] =
    for
        newGraph <- graph.addSamples(samples)
    yield this.copy(graph = newGraph)

  private[state] def getConForIoValue(ioValue: IoValue): F[(IoValue, Set[DcgNode.Concrete[F]])] =
    for
      mnIds <- ioValues.get(ioValue)
      nodes <- graph.getNodes[DcgNode.Concrete[F]](mnIds.map(_.asInstanceOf[MnId]))
    yield (ioValue, nodes.values.toSet)

  def findConForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList.traverse(getConForIoValue)
    yield (foundNodes.toMap, notFoundValues)

  override lazy val toString: String = s"DcgState(nodes count = ${graph.nodes.size}, edges count = ${graph.edges.size})"

object MapGraphState:
  def empty[F[_]: MonadThrow]: MapGraphState[F] = new MapGraphState[F](
    ioValues = IoValueMap.empty,
    graph = DcgGraph.empty
  )

  def apply[F[_]: MonadThrow](ioValues: IoValueMap[F], graph: DcgGraph[F]): F[MapGraphState[F]] =
    for
      _ <- graph.conMnId.assertSameElems(ioValues.allMnIds, "Con MnId in ioValues refer to unknown nodes in graph")
      _ <- graph.ioValues.assertSameElems(ioValues.keySet, "IoValues refer to unknown nodes in graph")
    yield new MapGraphState(ioValues, graph)

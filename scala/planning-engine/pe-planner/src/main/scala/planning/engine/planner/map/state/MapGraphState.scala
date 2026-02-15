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
import planning.engine.common.values.node.MnId
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.samples.DcgSample

final case class MapGraphState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[MnId.Con]],
    graph: DcgGraph[F]
):
  lazy val isEmpty: Boolean = ioValues.isEmpty && graph.isEmpty

  private[state] def updateOrAddIoValues(node: DcgNode.Concrete[F]): F[(IoValue, Set[MnId.Con])] =
    ioValues.get(node.ioValue) match
      case None                                => (node.ioValue -> Set(node.id)).pure
      case Some(ids) if !ids.contains(node.id) => (node.ioValue -> (ids + node.id)).pure
      case Some(ids) => s"Duplicate node id ${node.id} for IoValue ${node.ioValue}".assertionError

  def addNodes(nodes: Iterable[DcgNode[F]]): F[MapGraphState[F]] =
    for
      conNodes <- nodes.filter(_.isInstanceOf[DcgNode.Concrete[F]]).map(_.asInstanceOf[DcgNode.Concrete[F]]).pure
      updatedIoValues <- conNodes.toList.traverse(n => updateOrAddIoValues(n)).map(_.toMap)
      newGraph <- graph.addNodes(nodes)
    yield this.copy(
      ioValues = ioValues ++ updatedIoValues,
      graph = newGraph
    )

  def addSamples(samples: Iterable[DcgSample.Add[F]]): F[MapGraphState[F]] =
    for
        newGraph <- graph.addSamples(samples)
    yield this.copy(graph = newGraph)

  private[state] def getConForIoValue(ioValue: IoValue): F[(IoValue, Set[DcgNode.Concrete[F]])] = graph
    .getNodes[DcgNode.Concrete[F]](ioValues(ioValue).map(_.asInstanceOf[MnId]))
    .map(r => ioValue -> r.values.toSet)

  def findConForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList.traverse(getConForIoValue)
    yield (foundNodes.toMap, notFoundValues)

  override lazy val toString: String = s"DcgState(nodes count = ${graph.nodes.size}, edges count = ${graph.edges.size})"

object MapGraphState:
  def empty[F[_]: MonadThrow]: MapGraphState[F] = new MapGraphState[F](
    ioValues = Map.empty,
    graph = DcgGraph.empty[F]
  )

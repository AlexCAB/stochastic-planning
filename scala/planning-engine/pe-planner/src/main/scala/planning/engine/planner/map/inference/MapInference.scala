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
| created: 2026-03-06 |||||||||||*/

package planning.engine.planner.map.inference

import cats.effect.kernel.Async
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.graph.edges.EdgeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.data.ActiveAbsDag
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.state.MapGraphState

trait MapInferenceLike[F[_]]:
  def naiveInferActiveAbsDag(activeIds: Set[MnId.Con]): F[ActiveAbsDag[F]]

// This trait contains implementation of inference algorithms specific for the in-memory map.
trait MapInference[F[_]: {Async, LoggerFactory}] extends MapInferenceLike[F]:

  // Implementation in MapBaseLogic
  private[map] def getMapState: F[MapGraphState[F]]

  // This is naive initial approach (i.e. without probability calculation and local outcome joints)
  // based on idea of extracting active DAG (active DAG) from map graph,
  // by tracing LINK edges from active concrete nodes with filtering out inactive part
  // of graph (i.e. filtering out edges that not contain active samples).
  // Better algorithm should calculate nodes probability and filter out edges base one probability threshold.
  override def naiveInferActiveAbsDag(activeIds: Set[MnId.Con]): F[ActiveAbsDag[F]] =
    for
      graph <- getMapState.map(_.graph)
      activeSampleIds <- graph.findActiveSampleIds(activeIds.map(_.asMnId)).pure
      linkKeys <- graph.structure.traceAbsDagLayers(activeIds, graph.activeLinksFilter(activeSampleIds))
      mnIds = linkKeys.toSet.flatMap(_.flatMap(_.mnIds)) ++ activeIds
      linkEdges <- graph.getEdges[EdgeKey.Link](linkKeys.flatten.toSet)
      backThenKeys = graph.structure.findBackwardThenEdges(mnIds)
      nodes <- graph.getNodes[DcgNode[F]](mnIds)
      samples <- graph.getSamples(activeSampleIds)
      activeDag <- ActiveAbsDag(nodes.values, linkEdges.values, backThenKeys, samples.values)
    yield activeDag

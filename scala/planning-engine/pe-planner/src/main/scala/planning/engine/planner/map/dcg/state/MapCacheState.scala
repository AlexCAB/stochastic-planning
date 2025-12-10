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

package planning.engine.planner.map.dcg.state

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.edges.CachedEdge
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.nodes.{AbstractCachedNode, ConcreteCachedNode}

final case class MapCacheState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[HnId]],
    concreteNodes: Map[HnId, ConcreteCachedNode[F]],
    abstractNodes: Map[HnId, AbstractCachedNode[F]],
    edges: Map[(HnId, HnId), CachedEdge],
    forwardLinks: Map[HnId, Set[HnId]],
    backwardLinks: Map[HnId, Set[HnId]],
    forwardThen: Map[HnId, Set[HnId]],
    backwardThen: Map[HnId, Set[HnId]],
    samplesData: Map[SampleId, SampleData]
):
  def addConcreteNodes(nodes: List[ConcreteCachedNode[F]]): F[MapCacheState[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate Concrete Node IDs detected")
      groupedIoVals = nodes.groupBy(_.ioValue).view.mapValues(_.map(_.id).toSet)
      _ <- (ioValues.keySet, groupedIoVals.keySet).assertNoSameElems("Can't add IoValues that already exist")
      _ <- (concreteNodes.keySet, allNewHdId).assertNoSameElems("Can't add Concrete Nodes that already exist")
    yield this.copy(
      ioValues = ioValues ++ groupedIoVals,
      concreteNodes = concreteNodes ++ nodes.map(n => n.id -> n).toMap
    )

  def addEdges(nodes: List[CachedEdge]): F[MapCacheState[F]] = ???

  def addSamples(samples: List[SampleData]): F[MapCacheState[F]] =
    for
      sampleIds <- samples.map(_.id).pure
      _ <- sampleIds.assertDistinct("Duplicate Sample IDs detected")
      _ <- (sampleIds, samplesData.keySet).assertNoSameElems("Can't add Samples that already exist")
    yield this.copy(
      samplesData = samplesData ++ samples.map(s => s.id -> s).toMap
    )

  def getConcreteForHnId(id: HnId): F[ConcreteCachedNode[F]] = concreteNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"ConcreteCachedNode with HnId $id not found in cache".assertionError

  def getConcreteForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteCachedNode[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList
        .traverse(v => ioValues(v).toList.traverse(id => getConcreteForHnId(id)).map(n => v -> n.toSet))
    yield (foundNodes.toMap, notFoundValues)

object MapCacheState:
  def init[F[_]: MonadThrow](): MapCacheState[F] = new MapCacheState[F](
    ioValues = Map(),
    concreteNodes = Map(),
    abstractNodes = Map(),
    edges = Map(),
    forwardLinks = Map(),
    backwardLinks = Map(),
    forwardThen = Map(),
    backwardThen = Map(),
    samplesData = Map()
  )

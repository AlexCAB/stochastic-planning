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
import planning.engine.planner.map.dcg.edges.CachedEdge.Key
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.nodes.{AbstractCachedNode, ConcreteCachedNode}

final case class MapCacheState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[HnId]],
    concreteNodes: Map[HnId, ConcreteCachedNode[F]],
    abstractNodes: Map[HnId, AbstractCachedNode[F]],
    edges: Map[Key, CachedEdge],
    forwardLinks: Map[HnId, Set[HnId]],
    backwardLinks: Map[HnId, Set[HnId]],
    forwardThen: Map[HnId, Set[HnId]],
    backwardThen: Map[HnId, Set[HnId]],
    samplesData: Map[SampleId, SampleData]
):
  lazy val allHnIds: Set[HnId] = concreteNodes.keySet ++ abstractNodes.keySet

  private[map] def splitIds(newEdges: List[CachedEdge]): F[(Set[Key], Set[Key])] =
    newEdges.foldRight((Set[Key](), Set[Key]()).pure):
      case (e, acc) if e.key.edgeType.isLink => acc.map((ls, ts) => (ls + e.key, ts))
      case (e, acc) if e.key.edgeType.isThen => acc.map((ls, ts) => (ls, ts + e.key))
      case (e, _)                            => s"Edge with unsupported EdgeType detected, $e".assertionError

  private[map] def makeForward(keys: Set[Key]): Map[HnId, Set[HnId]] =
    keys.groupBy(_.sourceId).view.mapValues(_.map(_.targetId).toSet).toMap

  private[map] def makeBackward(keys: Set[Key]): Map[HnId, Set[HnId]] =
    keys.groupBy(_.targetId).view.mapValues(_.map(_.sourceId).toSet).toMap

  private[map] def joinIds(oldIds: Map[HnId, Set[HnId]], newIds: Map[HnId, Set[HnId]]): F[Map[HnId, Set[HnId]]] =
    newIds.foldRight(oldIds.pure):
      case ((hnId, targets), accF) => accF.flatMap:
          case acc if acc.contains(hnId) && acc(hnId).intersect(targets).isEmpty =>
            acc.updated(hnId, acc(hnId) ++ targets).pure
          case acc if !acc.contains(hnId) => (acc + (hnId -> targets)).pure
          case acc => s"Can't add duplicate links: $hnId -> ${acc(hnId).intersect(targets)}".assertionError

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

  def addEdges(newEdges: List[CachedEdge]): F[MapCacheState[F]] =
    for
      _ <- newEdges.map(_.key).assertDistinct("Duplicate Edge Keys detected")
      _ <- (allHnIds, newEdges.flatMap(_.hnIds)).assertContainsAll("Edge refers to unknown HnIds")
      (allLinkIds, allThenIds) <- splitIds(newEdges)
      nEdges = newEdges.map(e => e.key -> e).toMap
      _ <- (nEdges.keys, edges.keys).assertNoSameElems("Can't add Edges that already exist")
      nForwardLinks <- joinIds(forwardLinks, makeForward(allLinkIds))
      nBackwardLinks <- joinIds(backwardLinks, makeBackward(allLinkIds))
      nForwardThen <- joinIds(forwardThen, makeForward(allThenIds))
      nBackwardThen <- joinIds(backwardThen, makeBackward(allThenIds))
    yield this.copy(
      edges = edges ++ nEdges,
      forwardLinks = nForwardLinks,
      backwardLinks = nBackwardLinks,
      forwardThen = nForwardThen,
      backwardThen = nBackwardThen
    )

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

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
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.edges.DcgEdge.Key
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.nodes.{AbstractDcgNode, ConcreteDcgNode}

final case class DcgState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[HnId]],
    concreteNodes: Map[HnId, ConcreteDcgNode[F]],
    abstractNodes: Map[HnId, AbstractDcgNode[F]],
    edges: Map[Key, DcgEdge[F]],
    forwardLinks: Map[HnId, Set[HnId]],
    backwardLinks: Map[HnId, Set[HnId]],
    forwardThen: Map[HnId, Set[HnId]],
    backwardThen: Map[HnId, Set[HnId]],
    samplesData: Map[SampleId, SampleData]
):
  lazy val allHnIds: Set[HnId] = concreteNodes.keySet ++ abstractNodes.keySet
  lazy val allSampleIds: Set[SampleId] = samplesData.keySet
  
  lazy val isEmpty: Boolean =
    ioValues.isEmpty &&
    concreteNodes.isEmpty &&
    abstractNodes.isEmpty &&
    edges.isEmpty &&
    forwardLinks.isEmpty &&
    backwardLinks.isEmpty &&
    forwardThen.isEmpty &&
    backwardThen.isEmpty &&
    samplesData.isEmpty

  private[state] def checkEdges(edges: List[DcgEdge[F]]): F[Unit] =
    for
      _ <- edges.map(_.key).assertDistinct("Duplicate Edge Keys detected")
      _ <- (allHnIds, edges.flatMap(_.hnIds)).assertContainsAll("Edge refers to unknown HnIds")
      groupedSampleIds = edges.flatMap(e => e.samples.map((sId, _) => (sId, e.key.sourceId, e.key.targetId)))
        .groupBy(e => (e._2, e._3)).view.mapValues(_.map(_._1)).toList
      _ <- groupedSampleIds.traverse: (hnIds, sampleIds) =>
        sampleIds.assertDistinct(s"Duplicate SampleIds for Edge between HnIds $hnIds detected: $sampleIds")
    yield ()

  private[state] def splitKeys(newEdges: List[DcgEdge[F]]): F[(Set[Key], Set[Key])] =
    newEdges.foldRight((Set[Key](), Set[Key]()).pure):
      case (e, acc) if e.key.edgeType.isLink => acc.map((ls, ts) => (ls + e.key, ts))
      case (e, acc) if e.key.edgeType.isThen => acc.map((ls, ts) => (ls, ts + e.key))
      case (e, _)                            => s"Edge with unsupported EdgeType detected, $e".assertionError

  private[state] def makeForward(keys: Set[Key]): Map[HnId, Set[HnId]] =
    keys.groupBy(_.sourceId).view.mapValues(_.map(_.targetId).toSet).toMap

  private[state] def makeBackward(keys: Set[Key]): Map[HnId, Set[HnId]] =
    keys.groupBy(_.targetId).view.mapValues(_.map(_.sourceId).toSet).toMap

  private[state] def joinIds(oldIds: Map[HnId, Set[HnId]], newIds: Map[HnId, Set[HnId]]): F[Map[HnId, Set[HnId]]] =
    newIds.foldRight(oldIds.pure):
      case ((hnId, targets), accF) => accF.flatMap:
          case acc if acc.contains(hnId) && acc(hnId).intersect(targets).isEmpty =>
            acc.updated(hnId, acc(hnId) ++ targets).pure
          case acc if !acc.contains(hnId) => (acc + (hnId -> targets)).pure
          case acc => s"Can't add duplicate links: $hnId -> ${acc(hnId).intersect(targets)}".assertionError

  private[state] def joinEdges(oldEdges: Map[Key, DcgEdge[F]], newEdges: List[DcgEdge[F]]): F[Map[Key, DcgEdge[F]]] =
    newEdges.foldRight(oldEdges.pure)((nEdge, accF) =>
      for
        acc <- accF
        oEdge <- acc.get(nEdge.key).map(_.pure).getOrElse(s"Edge to merge not found for ${nEdge.key}".assertionError)
        mEdge <- oEdge.join(nEdge)
      yield acc.updated(nEdge.key, mEdge)
    )

  def addConcreteNodes(nodes: List[ConcreteDcgNode[F]]): F[DcgState[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate Concrete Node IDs detected")
      groupedIoVals = nodes.groupBy(_.ioValue).view.mapValues(_.map(_.id).toSet)
      _ <- (ioValues.keySet, groupedIoVals.keySet).assertNoSameElems("Can't add IoValues that already exist")
      _ <- (concreteNodes.keySet, allNewHdId).assertNoSameElems("Can't add concrete nodes that already exist")
    yield this.copy(
      ioValues = ioValues ++ groupedIoVals,
      concreteNodes = concreteNodes ++ nodes.map(n => n.id -> n).toMap
    )

  def addAbstractNodes(nodes: List[AbstractDcgNode[F]]): F[DcgState[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate abstract Node IDs detected")
      _ <- (abstractNodes.keySet, allNewHdId).assertNoSameElems("Can't add abstract nodes that already exist")
    yield this.copy(
      abstractNodes = abstractNodes ++ nodes.map(n => n.id -> n).toMap
    )

  def addEdges(newEdges: List[DcgEdge[F]]): F[DcgState[F]] =
    for
      _ <- checkEdges(newEdges)
      (allLinkIds, allThenIds) <- splitKeys(newEdges)
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

  def mergeEdges(list: List[DcgEdge[F]]): F[DcgState[F]] =
    for
      _ <- checkEdges(list)
      (inSetEdges, outSetEdges) = list.partition(e => edges.contains(e.key))
      joinedEdges <- joinEdges(edges, inSetEdges)
      newEdges = outSetEdges.map(e => e.key -> e).toMap
      _ <- (joinedEdges.keys, newEdges.keys).assertNoSameElems("Bug in partition of edges for merging")
      (allLinkIds, allThenIds) <- splitKeys(outSetEdges)
      nForwardLinks <- joinIds(forwardLinks, makeForward(allLinkIds))
      nBackwardLinks <- joinIds(backwardLinks, makeBackward(allLinkIds))
      nForwardThen <- joinIds(forwardThen, makeForward(allThenIds))
      nBackwardThen <- joinIds(backwardThen, makeBackward(allThenIds))
    yield this.copy(
      edges = joinedEdges ++ newEdges,
      forwardLinks = nForwardLinks,
      backwardLinks = nBackwardLinks,
      forwardThen = nForwardThen,
      backwardThen = nBackwardThen
    )

  def addSamples(samples: List[SampleData]): F[DcgState[F]] =
    for
      sampleIds <- samples.map(_.id).pure
      _ <- sampleIds.assertDistinct("Duplicate Sample IDs detected")
      _ <- (sampleIds, samplesData.keySet).assertNoSameElems("Can't add Samples that already exist")
    yield this.copy(
      samplesData = samplesData ++ samples.map(s => s.id -> s).toMap
    )

  def concreteForHnId(id: HnId): F[ConcreteDcgNode[F]] = concreteNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"ConcreteDcgNode with HnId $id not found in $concreteNodes".assertionError

  def abstractForHnId(id: HnId): F[AbstractDcgNode[F]] = abstractNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"AbstractDcgNode with HnId $id not found in $abstractNodes".assertionError

  def concreteForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList
        .traverse(v => ioValues(v).toList.traverse(id => concreteForHnId(id)).map(n => v -> n.toSet))
    yield (foundNodes.toMap, notFoundValues)

  def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, List[HnId]]] =
    for
      grouped <- (concreteNodes.values ++ abstractNodes.values).groupBy(_.name).pure
    yield names.map(n => n -> grouped.get(Some(n)).map(_.toList.map(_.id)).getOrElse(List())).toMap

object DcgState:
  def empty[F[_]: MonadThrow]: DcgState[F] = new DcgState[F](
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

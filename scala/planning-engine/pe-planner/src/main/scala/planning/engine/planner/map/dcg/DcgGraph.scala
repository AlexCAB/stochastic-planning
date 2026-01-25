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
| created: 2026-01-16 |||||||||||*/

package planning.engine.planner.map.dcg

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.planner.map.dcg.edges.{DcgEdgeData, DcgEdgesMapping}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation

final case class DcgGraph[F[_]: MonadThrow](
    concreteNodes: Map[HnId, DcgNode.Concrete[F]],
    abstractNodes: Map[HnId, DcgNode.Abstract[F]],
    edgesData: Map[EndIds, DcgEdgeData],
    edgesMapping: DcgEdgesMapping[F],
    samplesData: Map[SampleId, SampleData]
) extends Validation:

  lazy val allHnIds: Set[HnId] = concreteNodes.keySet ++ abstractNodes.keySet
  lazy val allSampleIds: Set[SampleId] = samplesData.keySet

  lazy val allIndexies: Map[HnId, Set[HnIndex]] = edgesData.values.toList
    .flatMap(e => List(e.ends.src -> e.srcHnIndex, e.ends.trg -> e.trgHnIndex))
    .groupBy(_._1)
    .map((hdId, lst) => hdId -> lst.flatMap(_._2).toSet)

  lazy val isEmpty: Boolean = concreteNodes.isEmpty &&
    abstractNodes.isEmpty &&
    edgesData.isEmpty &&
    edgesMapping.isEmpty &&
    samplesData.isEmpty

  override lazy val validationName: String = "DcgGraph"

  override lazy val validationErrors: List[Throwable] =
    val edgeAllHdIds = edgesData.values.flatMap(_.hnIds)
    val edgeSampleIds = edgesData.values.flatMap(_.sampleIds)

    edgesMapping.validationErrors ++ validations(
      concreteNodes.map((k, n) => k -> n.id).allEquals("Concrete nodes map keys and values IDs mismatch"),
      abstractNodes.map((k, n) => k -> n.id).allEquals("Abstract nodes map keys and values IDs mismatch"),
      concreteNodes.keySet.haveDifferentElems(abstractNodes.keySet, "Concrete and Abstract nodes IDs overlap detected"),
      edgesData.map((k, n) => k -> n.ends).allEquals("Edges data map keys and values ends mismatch"),
      allHnIds.containsAllOf(edgesData.values.flatMap(_.hnIds), "Edge refers to unknown HnIds"),
      allHnIds.containsAllOf(edgesMapping.allHnIds, "Edges mapping refers to unknown HnIds"),
      edgesData.keySet.haveSameElems(edgesMapping.allEnds, "Edges mapping refers to unknown edge ends"),
      samplesData.map((k, n) => k -> n.id).allEquals("Samples data map keys and values IDs mismatch"),
      allSampleIds.containsAllOf(edgeSampleIds, "Some sample IDs used in edges are not found")
    )

  private[map] def checkEdges(edges: Iterable[DcgEdgeData]): F[Unit] =
    for
      _ <- edges.map(_.ends).assertDistinct("Duplicate Edge Keys detected")
      _ <- allHnIds.assertContainsAll(edges.flatMap(_.hnIds), "Edge refers to unknown HnIds")
    yield ()

  private[map] def joinEdges(
      oldEdges: Map[EndIds, DcgEdgeData],
      newEdges: Iterable[DcgEdgeData]
  ): F[Map[EndIds, DcgEdgeData]] = newEdges.foldRight(oldEdges.pure)((nEdge, accF) =>
    for
      acc <- accF
      oEdge <- acc.get(nEdge.ends).map(_.pure).getOrElse(s"Edge to merge not found for ${nEdge.ends}".assertionError)
      mEdge <- oEdge.join(nEdge)
    yield acc.updated(nEdge.ends, mEdge)
  )

  private[map] def getEdges(ends: Set[EndIds]): F[Map[EndIds, DcgEdgeData]] =
    for
      filteredEdges <- edgesData.filter((e, _) => ends.contains(e)).pure
      _ <- ends.assertSameElems(filteredEdges.keySet, "Bug: All ends in mapping should be in edges data")
    yield filteredEdges

  private[map] def nextHnIndexies(hdIds: Set[HnId]): F[Map[HnId, HnIndex]] =
    for
      _ <- allHnIds.assertContainsAll(hdIds, "Some HnIds are not found in the graph")
      filteredIndexies = allIndexies.filter((hdId, _) => hdIds.contains(hdId))
      maxIndexies = filteredIndexies.map((hdId, lst) => hdId -> lst.map(_.value).maxOption)
      nextIndexies = maxIndexies.map((hdId, opMax) => hdId -> opMax.map(i => HnIndex(i + 1)).getOrElse(HnIndex.init))
    yield nextIndexies

  def addConNodes(nodes: Iterable[DcgNode.Concrete[F]]): F[DcgGraph[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate Concrete Node IDs detected")
      _ <- concreteNodes.keySet.assertNoSameElems(allNewHdId, "Can't add concrete nodes that already exist")
    yield this.copy(concreteNodes = concreteNodes ++ nodes.map(n => n.id -> n).toMap)

  def addAbsNodes(nodes: Iterable[DcgNode.Abstract[F]]): F[DcgGraph[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate abstract Node IDs detected")
      _ <- abstractNodes.keySet.assertNoSameElems(allNewHdId, "Can't add abstract nodes that already exist")
    yield this.copy(abstractNodes = abstractNodes ++ nodes.map(n => n.id -> n).toMap)

  def addEdges(newEdges: Iterable[DcgEdgeData]): F[DcgGraph[F]] =
    for
      _ <- checkEdges(newEdges)
      nEdges = newEdges.map(e => e.ends -> e).toMap
      _ <- nEdges.keys.assertNoSameElems(edgesData.keys, "Can't add Edges that already exist")
      nEdgesMapping <- edgesMapping.addAll(nEdges.keySet)
    yield this.copy(edgesData = edgesData ++ nEdges, edgesMapping = nEdgesMapping)

  def mergeEdges(list: Iterable[DcgEdgeData]): F[DcgGraph[F]] =
    for
      _ <- checkEdges(list)
      (inSetEdges, outSetEdges) = list.partition(e => edgesData.contains(e.ends))
      joinedEdges <- joinEdges(edgesData, inSetEdges)
      newEdges = outSetEdges.map(e => e.ends -> e).toMap
      _ <- joinedEdges.keys.assertNoSameElems(newEdges.keys, "Bug in partition of edges for merging")
      nEdgesMapping <- edgesMapping.addAll(newEdges.keySet)
    yield this.copy(edgesData = joinedEdges ++ newEdges, edgesMapping = nEdgesMapping)

  def addSamples(samples: Iterable[SampleData]): F[DcgGraph[F]] =
    for
      sampleIds <- samples.map(_.id).pure
      _ <- sampleIds.assertDistinct("Duplicate Sample IDs detected")
      _ <- sampleIds.assertNoSameElems(samplesData.keySet, "Can't add Samples that already exist")
    yield this.copy(samplesData = samplesData ++ samples.map(s => s.id -> s).toMap)

  def getConForHnId(id: HnId): F[DcgNode.Concrete[F]] = concreteNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"DcgNode.Concrete with HnId $id not found in ${concreteNodes.keySet}".assertionError

  def getConForHnIds(ids: Set[HnId]): F[Map[HnId, DcgNode.Concrete[F]]] =
    for
      found <- concreteNodes.filter((id, _) => ids.contains(id)).pure
      _ <- found.keySet.assertContainsAll(ids, "Some concrete node IDs are not found")
    yield found

  def getAbsForHnId(id: HnId): F[DcgNode.Abstract[F]] = abstractNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"DcgNode.Abstract with HnId $id not found in ${abstractNodes.keySet}".assertionError

  def getAbsForHnIds(ids: Set[HnId]): F[Map[HnId, DcgNode.Abstract[F]]] =
    for
      found <- abstractNodes.filter((id, _) => ids.contains(id)).pure
      _ <- found.keySet.assertContainsAll(ids, "Some abstract node IDs are not found")
    yield found

  def getSamples(sampleIds: Set[SampleId]): F[Map[SampleId, SampleData]] =
    for
      found <- samplesData.filter((id, _) => sampleIds.contains(id)).pure
      _ <- found.keySet.assertContainsAll(sampleIds, "Bug: Some sample IDs are not found")
    yield found

  def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]] =
    for
      allNodes <- (concreteNodes.values ++ abstractNodes.values).pure[F]
      grouped = allNodes.filter(n => n.name.isDefined && names.contains(n.name.get)).groupBy(_.name.get)
    yield grouped.view.mapValues(_.map(_.id).toSet).toMap

  def findForwardLinkEdges(sourceHnIds: Set[HnId]): F[Map[EndIds, DcgEdgeData]] =
    for
      ends <- getEdges(edgesMapping.findForward(sourceHnIds)).map(_.filter(_._2.isLink))
      _ <- sourceHnIds.assertSameElems(ends.keys.map(_.src), "Found ends must have as source one of given HnIds")
    yield ends

  def findForwardActiveLinkEdges(sourceHnIds: Set[HnId], activeSampleIds: Set[SampleId]): F[Map[EndIds, DcgEdgeData]] =
    findForwardLinkEdges(sourceHnIds).map(_.filter((_, edge) => edge.linksIds.exists(activeSampleIds.contains)))

  def findBackwardThenEdges(targetHnIds: Set[HnId]): F[Map[EndIds, DcgEdgeData]] =
    for
      ends <- getEdges(edgesMapping.findBackward(targetHnIds)).map(_.filter(_._2.isThen))
      _ <- targetHnIds.assertSameElems(ends.keys.map(_.trg), "Found ends must have as target one of given HnIds")
    yield ends

  def findBackwardActiveThenEdges(targetHnIds: Set[HnId], activeSampleIds: Set[SampleId]): F[Map[EndIds, DcgEdgeData]] =
    findBackwardThenEdges(targetHnIds).map(_.filter((_, edge) => edge.thensIds.exists(activeSampleIds.contains)))

  def addSample(data: SampleData, edges: Set[(EdgeType, EndIds)]): F[DcgGraph[F]] = ???

object DcgGraph:
  def empty[F[_]: MonadThrow]: DcgGraph[F] = DcgGraph[F](
    concreteNodes = Map.empty,
    abstractNodes = Map.empty,
    edgesData = Map.empty,
    edgesMapping = DcgEdgesMapping.empty,
    samplesData = Map.empty
  )

  def apply[F[_]: MonadThrow](
      concreteNodes: Iterable[DcgNode.Concrete[F]],
      abstractNodes: Iterable[DcgNode.Abstract[F]],
      edgesData: Iterable[DcgEdgeData],
      samplesData: Iterable[SampleData]
  ): F[DcgGraph[F]] =
    for
      _ <- concreteNodes.map(_.id).assertDistinct("Duplicate Concrete Node IDs detected")
      _ <- abstractNodes.map(_.id).assertDistinct("Duplicate Abstract Node IDs detected")
      _ <- edgesData.map(_.ends).assertDistinct("Duplicate Edge Keys detected")
      allHnIds = concreteNodes.map(_.id).toSet ++ abstractNodes.map(_.id).toSet
      _ <- allHnIds.assertContainsAll(edgesData.flatMap(_.hnIds), "Edge refers to unknown HnIds")
      allSampleIds = edgesData.flatMap(_.sampleIds)
      _ <- samplesData.map(_.id).assertContainsAll(allSampleIds, "Some sample IDs used in edges are not found")
    yield DcgGraph(
      concreteNodes = concreteNodes.map(n => n.id -> n).toMap,
      abstractNodes = abstractNodes.map(n => n.id -> n).toMap,
      edgesData = edgesData.map(e => e.ends -> e).toMap,
      edgesMapping = DcgEdgesMapping(edgesData.map(_.ends)),
      samplesData = samplesData.map(s => s.id -> s).toMap
    )

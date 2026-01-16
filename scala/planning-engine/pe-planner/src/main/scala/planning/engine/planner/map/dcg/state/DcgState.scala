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
import planning.engine.planner.map.dcg.edges.{DcgEdgeData, DcgEdgesMapping}
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.nodes.DcgNode

final case class DcgState[F[_]: MonadThrow](
    ioValues: Map[IoValue, Set[HnId]],
    concreteNodes: Map[HnId, DcgNode.Concrete[F]],
    abstractNodes: Map[HnId, DcgNode.Abstract[F]],
    edgesData: Map[EndIds, DcgEdgeData],
    edgesMapping: DcgEdgesMapping,
    samplesData: Map[SampleId, SampleData]
):
  lazy val allHnIds: Set[HnId] = concreteNodes.keySet ++ abstractNodes.keySet
  lazy val allSampleIds: Set[SampleId] = samplesData.keySet

  lazy val isEmpty: Boolean = ioValues.isEmpty &&
    concreteNodes.isEmpty &&
    abstractNodes.isEmpty &&
    edgesData.isEmpty &&
    edgesMapping.isEmpty &&
    samplesData.isEmpty

  private[state] def checkEdges(edges: List[DcgEdgeData]): F[Unit] =
    for
      _ <- edges.map(_.ends).assertDistinct("Duplicate Edge Keys detected")
      _ <- (allHnIds, edges.flatMap(_.hnIds)).assertContainsAll("Edge refers to unknown HnIds")
    yield ()

  private[state] def joinEdges(
      oldEdges: Map[EndIds, DcgEdgeData],
      newEdges: List[DcgEdgeData]
  ): F[Map[EndIds, DcgEdgeData]] = newEdges.foldRight(oldEdges.pure)((nEdge, accF) =>
    for
      acc <- accF
      oEdge <- acc.get(nEdge.ends).map(_.pure).getOrElse(s"Edge to merge not found for ${nEdge.ends}".assertionError)
      mEdge <- oEdge.join(nEdge)
    yield acc.updated(nEdge.ends, mEdge)
  )

  def addConcreteNodes(nodes: List[DcgNode.Concrete[F]]): F[DcgState[F]] =
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

  def addAbstractNodes(nodes: List[DcgNode.Abstract[F]]): F[DcgState[F]] =
    for
      allNewHdId <- nodes.map(_.id).pure
      _ <- allNewHdId.assertDistinct("Duplicate abstract Node IDs detected")
      _ <- (abstractNodes.keySet, allNewHdId).assertNoSameElems("Can't add abstract nodes that already exist")
    yield this.copy(
      abstractNodes = abstractNodes ++ nodes.map(n => n.id -> n).toMap
    )

  def addEdges(newEdges: List[DcgEdgeData]): F[DcgState[F]] =
    for
      _ <- checkEdges(newEdges)
      nEdges = newEdges.map(e => e.ends -> e).toMap
      _ <- (nEdges.keys, edgesData.keys).assertNoSameElems("Can't add Edges that already exist")
      nEdgesMapping <- edgesMapping.addAll(nEdges.keySet)
    yield this.copy(
      edgesData = edgesData ++ nEdges,
      edgesMapping = nEdgesMapping
    )

  def mergeEdges(list: List[DcgEdgeData]): F[DcgState[F]] =
    for
      _ <- checkEdges(list)
      (inSetEdges, outSetEdges) = list.partition(e => edgesData.contains(e.ends))
      joinedEdges <- joinEdges(edgesData, inSetEdges)
      newEdges = outSetEdges.map(e => e.ends -> e).toMap
      _ <- (joinedEdges.keys, newEdges.keys).assertNoSameElems("Bug in partition of edges for merging")
      nEdgesMapping <- edgesMapping.addAll(newEdges.keySet)
    yield this.copy(
      edgesData = joinedEdges ++ newEdges,
      edgesMapping = nEdgesMapping
    )

  def addSamples(samples: List[SampleData]): F[DcgState[F]] =
    for
      sampleIds <- samples.map(_.id).pure
      _ <- sampleIds.assertDistinct("Duplicate Sample IDs detected")
      _ <- (sampleIds, samplesData.keySet).assertNoSameElems("Can't add Samples that already exist")
    yield this.copy(
      samplesData = samplesData ++ samples.map(s => s.id -> s).toMap
    )

  def concreteForHnId(id: HnId): F[DcgNode.Concrete[F]] = concreteNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"DcgNode.Concrete with HnId $id not found in $concreteNodes".assertionError

  def abstractForHnId(id: HnId): F[DcgNode.Abstract[F]] = abstractNodes.get(id) match
    case Some(node) => node.pure
    case None       => s"DcgNode.Abstract with HnId $id not found in $abstractNodes".assertionError

  def concreteForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      (found, notFoundValues) <- values.partition(ioValues.contains).pure
      foundNodes <- found.toList
        .traverse(v => ioValues(v).toList.traverse(id => concreteForHnId(id)).map(n => v -> n.toSet))
    yield (foundNodes.toMap, notFoundValues)

  def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]] =
    for
      allNodes <- (concreteNodes.values ++ abstractNodes.values).pure[F]
      grouped = allNodes.filter(n => n.name.isDefined && names.contains(n.name.get)).groupBy(_.name.get)
    yield grouped.view.mapValues(_.map(_.id).toSet).toMap

  override def toString: String =
    s"DcgState(concreteNodes = ${concreteNodes.keys}, abstractNodes = ${abstractNodes.keys})"

object DcgState:
  def empty[F[_]: MonadThrow]: DcgState[F] = new DcgState[F](
    ioValues = Map.empty,
    concreteNodes = Map.empty,
    abstractNodes = Map.empty,
    edgesData = Map.empty,
    edgesMapping = DcgEdgesMapping.empty,
    samplesData = Map.empty
  )

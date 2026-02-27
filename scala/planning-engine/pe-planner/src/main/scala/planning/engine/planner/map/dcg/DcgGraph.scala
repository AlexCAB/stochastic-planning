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
import planning.engine.common.values.node.{HnIndex, HnName, MnId}
import planning.engine.common.values.node.MnId.{filterCon, filterAbs}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.common.values.edge.{EdgeKey, IndexMap}
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.common.errors.*
import planning.engine.common.graph.GraphStructure
import planning.engine.common.values.io.IoValue
import planning.engine.planner.map.dcg.samples.DcgSample

import scala.reflect.ClassTag

final case class DcgGraph[F[_]: MonadThrow](
    nodes: Map[MnId, DcgNode[F]],
    edges: Map[EdgeKey, DcgEdge[F]],
    samples: Map[SampleId, SampleData],
    structure: GraphStructure[F]
):
  lazy val mnIds: Set[MnId] = nodes.keySet
  lazy val sampleIds: Set[SampleId] = samples.keySet
  lazy val edgesMdIds: Set[MnId] = edges.values.flatMap(_.mnIds).toSet
  lazy val ioValues: Set[IoValue] = nodes.values.flatMap(_.asConcrete.map(_.ioValue)).toSet
  lazy val isEmpty: Boolean = nodes.isEmpty

  lazy val conMnId: Set[MnId.Con] = nodes.keySet.filterCon
  lazy val absMnId: Set[MnId.Abs] = nodes.keySet.filterAbs

  // Map of all HnIndexes for each MnId, collected from all edges in the graph.
  // Used for validating new edges in samples added to the graph, since only validation in DcgEdge.DcgSamples
  // is not enough, because some HnIndexes can be used in different edges.
  lazy val allIndexies: Map[MnId, Set[HnIndex]] = edges.values
    .flatMap(e => List(e.key.src -> e.samples.srcHnIndex, e.key.trg -> e.samples.trgHnIndex))
    .groupBy(_._1)
    .map((mnId, lst) => mnId -> lst.flatMap(_._2).toSet)

  def getNodes[N <: DcgNode[F]: ClassTag](ids: Set[MnId]): F[Map[MnId, N]] =
    val rc = implicitly[ClassTag[N]].runtimeClass
    for
      found <- nodes.filter((id, n) => ids.contains(id) && rc.isInstance(n)).pure
      _ <- found.keySet.assertContainsAllOf(ids, "Some node IDs are not found")
    yield found.map((k, v) => k -> v.asInstanceOf[N])

  def getEdges[K <: EdgeKey: ClassTag](keys: Set[EdgeKey]): F[Map[K, DcgEdge[F]]] =
    val rc = implicitly[ClassTag[K]].runtimeClass
    for
      edges <- this.edges.filter((k, _) => keys.contains(k) && rc.isInstance(k)).pure
      _ <- keys.assertSameElems(edges.keySet, "Some edge keys are not found in the graph")
    yield edges.map((k, v) => k.asInstanceOf[K] -> v)

  def getSamples(sampleIds: Set[SampleId]): F[Map[SampleId, SampleData]] =
    for
      found <- samples.filter((id, _) => sampleIds.contains(id)).pure
      _ <- found.keySet.assertContainsAllOf(sampleIds, "Bug: Some sample IDs are not found")
    yield found

  // Add new nodes to the graph, checking that their IDs are distinct and do not already exist in the graph
  def addNodes(nodes: Iterable[DcgNode[F]]): F[DcgGraph[F]] =
    for
      ids <- nodes.map(_.id).pure
      _ <- ids.assertDistinct("Duplicate Node IDs detected")
      _ <- mnIds.assertContainsNoneOf(ids, "Can't add nodes that already exist")
    yield this.copy(nodes = this.nodes ++ nodes.map(n => n.id -> n).toMap)

  private[map] def checkHnIndex(mnId: MnId, indexies: Set[HnIndex]): F[Unit] = allIndexies
    .get(mnId)
    .map(_.assertContainsNoneOf(indexies, s"Duplicate indexes for MnId $mnId"))
    .getOrElse(().pure)

  private[map] def updateOrAddEdge(key: EdgeKey, indexies: Map[SampleId, IndexMap]): F[DcgEdge[F]] =
    for
      newEdge <- DcgEdge(key, indexies)
      _ <- mnIds.assertContainsAllOf(Set(key.src, key.trg), s"Edge key $key refers to unknown MnIds")
      _ <- checkHnIndex(key.src, newEdge.samples.srcHnIndex)
      _ <- checkHnIndex(key.trg, newEdge.samples.trgHnIndex)
      edge <- edges.get(key).map(_.join(newEdge)).getOrElse(newEdge.pure)
    yield edge

  // Add a new DCG samples to the graph. For each edge in sample if exist edge with the same key, add sample to it,
  // otherwise create new DcgEdge.
  def addSamples(samples: Iterable[DcgSample.Add[F]]): F[DcgGraph[F]] =
    for
      sampleIds <- samples.map(_.sample.data.id).pure
      _ <- sampleIds.assertDistinct("Duplicate sample IDs detected")
      _ <- sampleIds.assertContainsNoneOf(this.sampleIds, s"Some sample IDs already exists in the graph")
      sampleIdsByKeys = samples.flatMap(_.idsByKey).groupBy(_._1).view.mapValues(_.map(_._2).toMap).toList
      updatedEdges <- sampleIdsByKeys.traverse((k, ids) => updateOrAddEdge(k, ids).map(e => k -> e)).map(_.toMap)
      addedSamples = samples.map(s => s.sample.data.id -> s.sample.data).toMap
      newStructure <- structure.add(updatedEdges.keySet.filterNot(k => edges.contains(k)))
    yield this.copy(
      edges = this.edges ++ updatedEdges,
      samples = this.samples ++ addedSamples,
      structure = newStructure
    )

  def findHnIdsByNames(names: Set[HnName]): Map[HnName, Set[MnId]] = nodes.values
    .filter(_.name.exists(name => names.contains(name)))
    .groupBy(_.name.get)
    .view.mapValues(_.map(_.id).toSet)
    .toMap

  def findForwardLinkEdges(srcMnIds: Set[MnId]): F[Map[EdgeKey.Link, DcgEdge[F]]] =
    getEdges[EdgeKey.Link](structure.findForward(srcMnIds).filter(_.isInstanceOf[EdgeKey.Link]))

  def findForwardActiveLinkEdges(srcMnIds: Set[MnId], sampleIds: Set[SampleId]): F[Map[EdgeKey.Link, DcgEdge[F]]] =
    findForwardLinkEdges(srcMnIds).map(_.filter((_, e) => e.isActive(sampleIds)))

  def findBackwardThenEdges(trgHnIds: Set[MnId]): F[Map[EdgeKey.Then, DcgEdge[F]]] =
    getEdges[EdgeKey.Then](structure.findBackward(trgHnIds).filter(_.isInstanceOf[EdgeKey.Then]))

  def findBackwardActiveThenEdges(trgMnIds: Set[MnId], sampleIds: Set[SampleId]): F[Map[EdgeKey.Then, DcgEdge[F]]] =
    findBackwardThenEdges(trgMnIds).map(_.filter((_, e) => e.isActive(sampleIds)))

  override lazy val toString: String =
    s"""DcgGraph(
       | nodes count: ${nodes.size}
       | edges count: ${edges.size}
       | samples count: ${samples.size}
       |)""".stripMargin

object DcgGraph:
  def empty[F[_]: MonadThrow]: DcgGraph[F] = new DcgGraph[F](
    nodes = Map.empty,
    edges = Map.empty,
    samples = Map.empty,
    structure = GraphStructure.empty
  )

  def apply[F[_]: MonadThrow](
      nodes: Iterable[DcgNode[F]],
      edges: Iterable[DcgEdge[F]],
      samples: Iterable[SampleData]
  ): F[DcgGraph[F]] =
    for
      _ <- nodes.map(_.id).assertDistinct("Duplicate node IDs detected")
      _ <- edges.map(_.key).assertDistinct("Duplicate Edge Keys detected")
      nodesMap = nodes.map(n => n.id -> n).toMap
      edgesMap = edges.map(e => e.key -> e).toMap
      _ <- nodesMap.keySet.assertContainsAllOf(edges.flatMap(_.mnIds), "Edge refers to unknown HnIds")
      samplesMap = samples.map(s => s.id -> s).toMap
      edgesSampleIds = edges.flatMap(_.samples.sampleIds)
      _ <- samplesMap.keySet.assertContainsAllOf(edgesSampleIds, "Some sample IDs used in edges are not found")
    yield new DcgGraph(nodesMap, edgesMap, samplesMap, GraphStructure(edgesMap.keySet))

  def apply[F[_]: MonadThrow](
      nodes: Map[MnId, DcgNode[F]],
      edges: Map[EdgeKey, DcgEdge[F]],
      samples: Map[SampleId, SampleData],
      structure: GraphStructure[F]
  ): F[DcgGraph[F]] =
    for
      mnIds <- nodes.keySet.pure
      sampleIds = samples.keySet
      edgeSampleIds = edges.values.flatMap(_.samples.sampleIds)
      _ <- mnIds.assertSameElems(nodes.values.map(_.id), "Nodes map keys and values IDs mismatch")
      _ <- edges.keySet.assertSameElems(edges.values.map(_.key), "Edges data map keys and values key mismatch")
      _ <- sampleIds.assertSameElems(samples.values.map(_.id), "Samples data map keys and values IDs mismatch")
      _ <- mnIds.assertContainsAllOf(edges.values.flatMap(_.mnIds), "Edge refers to unknown MnIds")
      _ <- mnIds.assertContainsAllOf(structure.mnIds, "Graph structure refers to unknown MnIds")
      _ <- structure.keys.assertSameElems(edges.keySet, "Graph structure refers to unknown edge key")
      _ <- sampleIds.assertContainsAllOf(edgeSampleIds, "Some sample IDs used in edges are not found")
    yield new DcgGraph(nodes, edges, samples, structure)

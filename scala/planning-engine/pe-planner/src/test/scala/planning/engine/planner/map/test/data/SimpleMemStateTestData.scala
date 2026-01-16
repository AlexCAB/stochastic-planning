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
| created: 2025-12-15 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.io.{IoIndex, IoValue}
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.samples.sample.Sample
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.edges.{DcgEdgeData, DcgEdgesMapping}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.state.{DcgState, MapInfoState}

trait SimpleMemStateTestData extends MapNodeTestData with MapSampleTestData with MapDcgNodeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testNotInMap = IoValue(testBoolInNode.name, IoIndex(-1))

  lazy val hnId1 = HnId(1)
  lazy val hnId2 = HnId(2)
  lazy val hnId3 = HnId(3)
  lazy val hnId4 = HnId(4)
  lazy val allHnId = Set(hnId1, hnId2, hnId3, hnId4)

  lazy val sampleId1 = SampleId(1001)
  lazy val sampleId2 = SampleId(1002)
  lazy val sampleId3 = SampleId(1003)
  lazy val sampleId4 = SampleId(1004)
  lazy val sampleId5 = SampleId(1005)

  lazy val conNodes = List(hnId1, hnId2).map(id => makeConcreteNode(id = id))
  lazy val absNodes = List(hnId3, hnId4).map(id => makeAbstractNode(id = id))

  lazy val conDcgNodes = conNodes.map(n => DcgNode.Concrete(n).unsafeRunSync())
  lazy val absDcgNodes = absNodes.map(n => DcgNode.Abstract(n).unsafeRunSync())

  lazy val initSamples = List(
    makeSample(sampleId1, hnId1, hnId2),
    makeSample(sampleId2, hnId2, hnId1),
    makeSample(sampleId3, hnId2, hnId1)
  )

  lazy val newSamples = List(
    makeSample(sampleId4, hnId2, hnId1),
    makeSample(sampleId5, hnId3, hnId4)
  )

  lazy val initialDcgState: DcgState[IO] = DcgState.empty[IO]
    .addAbstractNodes(absDcgNodes)
    .flatMap(_.addConcreteNodes(conDcgNodes))
    .unsafeRunSync()

  lazy val sampleInd1 = SampleIndexies(sampleId1, HnIndex(201), HnIndex(202))
  lazy val sampleInd2 = SampleIndexies(sampleId2, HnIndex(203), HnIndex(204))

  lazy val mapSubGraph = MapSubGraph[IO](
    concreteNodes = conNodes,
    abstractNodes = List(),
    edges = makeHiddenEdges(initSamples),
    skippedSamples = List(),
    loadedSamples = initSamples.map(_.data)
  )

  lazy val ioValues = mapSubGraph.concreteNodes.map(_.ioValue)
  lazy val conDcgNodesMap = conDcgNodes.map(n => n.ioValue -> Set(n)).toMap
  lazy val dcgEdges = mapSubGraph.edges.map(DcgEdgeData.apply)

  private def references(isForward: Boolean): Map[HnId, Set[HnId]] = dcgEdges
    .map(_.ends)
    .groupBy(k => if isForward then k.src else k.trg)
    .view.mapValues(_.map(k => if isForward then k.trg else k.src).toSet)
    .toMap

  lazy val dcgStateFromSubGraph = DcgState[IO](
    ioValues = conDcgNodesMap.map((k, ns) => k -> ns.map(_.id)),
    concreteNodes = conDcgNodesMap.flatMap((_, ns) => ns.map(n => n.id -> n)),
    abstractNodes = Map.empty,
    edgesData = dcgEdges.map(e => e.ends -> e).toMap,
    edgesMapping = DcgEdgesMapping(
      forward = references(isForward = true),
      backward = references(isForward = false)
    ),
    samplesData = mapSubGraph.loadedSamples.map(s => s.id -> s).toMap
  )

  lazy val sampleListNew = Sample.ListNew(List(
    makeNewSampleData(hnId1, hnId2, name = Name.some("New Sample 01")),
    makeNewSampleData(hnId2, hnId3, name = Name.some("New Sample 02"))
  ))

  lazy val concreteNodesNew = ConcreteNode
    .ListNew(conNodes.map(n => ConcreteNode.New(n.name, n.description, n.ioNode.name, n.valueIndex)))

  lazy val abstractNodesNew = AbstractNode
    .ListNew(absNodes.map(n => AbstractNode.New(n.name, n.description)))

  lazy val testMapInfoState: MapInfoState[IO] = MapInfoState[IO](
    metadata = testMetadata,
    inNodes = testInNodes.map(n => n.name -> n).toMap,
    outNodes = testOutNodes.map(n => n.name -> n).toMap
  )

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
import planning.engine.common.values.io.{IoIndex, IoValue, IoValueMap}
import planning.engine.common.values.node.{HnId, MnId, HnIndex}
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.samples.sample.Sample
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}

trait MapTestData extends MapNodeTestData with MapSampleTestData with DcgGraphTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testNotInMap = IoValue(testBoolInNode.name, IoIndex(-1))

  lazy val hnId1 = HnId(1)
  lazy val hnId2 = HnId(2)
  lazy val hnId3 = HnId(3)
  lazy val hnId4 = HnId(4)
  lazy val allHnId: Set[HnId] = Set(hnId1, hnId2, hnId3, hnId4)

  lazy val mapConNodes: List[ConcreteNode[IO]] = List(hnId1, hnId2).map(id => makeConcreteNode(id = id))
  lazy val mapAbsNodes: List[AbstractNode[IO]] = List(hnId3, hnId4).map(id => makeAbstractNode(id = id))

  lazy val conDcgNodes: List[DcgNode.Concrete[IO]] = mapConNodes.map(n => DcgNode.Concrete(n).unsafeRunSync())
  lazy val absDcgNodes: List[DcgNode.Abstract[IO]] = mapAbsNodes.map(n => DcgNode.Abstract(n).unsafeRunSync())
  lazy val allDcgNodes: List[DcgNode[IO]] = conDcgNodes ++ absDcgNodes

  lazy val conDcgMnId: Set[MnId.Con] = conDcgNodes.map(_.id).toSet
  lazy val absDcgMnId: Set[MnId.Abs] = absDcgNodes.map(_.id).toSet
  
  def  hnIdToMnId(id: HnId): MnId = id.toMnId[IO](conDcgMnId, absDcgMnId).unsafeRunSync()

  lazy val initSamples: List[Sample] = List(
    makeSample(sampleId1, hnId1, hnId2),
    makeSample(sampleId2, hnId2, hnId1),
    makeSample(sampleId3, hnId2, hnId1)
  )

  lazy val newSamples = List(
    makeSample(sampleId4, hnId2, hnId1),
    makeSample(sampleId5, hnId3, hnId4)
  )

  lazy val initialDcgState = MapGraphState.empty[IO]
    .addNodes(allDcgNodes)
    .unsafeRunSync()

  lazy val sampleInd1 = SampleIndexies(sampleId1, HnIndex(201), HnIndex(202))
  lazy val sampleInd2 = SampleIndexies(sampleId2, HnIndex(203), HnIndex(204))

  lazy val mapSubGraph = MapSubGraph[IO](
    concreteNodes = mapConNodes,
    abstractNodes = List(),
    edges = makeHiddenEdges(initSamples),
    skippedSamples = List(),
    loadedSamples = initSamples.map(_.data)
  )

  lazy val ioValues: List[IoValue] = mapSubGraph.concreteNodes.map(_.ioValue)
  lazy val conDcgNodesMap: Map[IoValue, Set[DcgNode.Concrete[IO]]] = conDcgNodes.map(n => n.ioValue -> Set(n)).toMap

  lazy val mapDcgEdges: List[DcgEdge[IO]] = mapSubGraph.edges
    .map(e => DcgEdge.apply[IO](e, conDcgNodes.map(_.id).toSet, absDcgNodes.map(_.id).toSet).unsafeRunSync())

  lazy val dcgStateFromSubGraph = MapGraphState[IO](
    ioValues = IoValueMap[IO](conDcgNodesMap.map((k, ns) => k -> ns.map(_.id))).unsafeRunSync(),
    graph = DcgGraph[IO](allDcgNodes, mapDcgEdges, mapSubGraph.loadedSamples).unsafeRunSync()
  ).unsafeRunSync()

  lazy val sampleListNew = Sample.ListNew(List(
    makeNewSampleData(hnId1, hnId2, name = Name.some("New Sample 01")),
    makeNewSampleData(hnId2, hnId3, name = Name.some("New Sample 02"))
  ))

  lazy val concreteNodesNew = ConcreteNode
    .ListNew(mapConNodes.map(n => ConcreteNode.New(n.name, n.description, n.ioNode.name, n.valueIndex)))

  lazy val abstractNodesNew = AbstractNode
    .ListNew(mapAbsNodes.map(n => AbstractNode.New(n.name, n.description)))

  lazy val testMapInfoState: MapInfoState[IO] = MapInfoState[IO](
    metadata = testMetadata,
    inNodes = testInNodes.map(n => n.name -> n).toMap,
    outNodes = testOutNodes.map(n => n.name -> n).toMap
  )

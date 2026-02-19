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
| created: 2025-07-11 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.Json
import planning.engine.api.model.map.payload.*
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.GraphStructure
import planning.engine.common.values.db.DbName
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnIndex, HnName, MnId}
import planning.engine.map.config.MapConfig
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.*
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.common.values.edge.{EdgeKey, Indexies}
import planning.engine.planner.map.dcg.nodes.*
import planning.engine.planner.map.dcg.edges.{DcgEdge, DcgSamples}
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}

trait TestApiData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testConfig: MapConfig = MapConfig(
    initNextHnId = 100L,
    initNextSampleId = 200L,
    initSampleCount = 300L,
    initNextHnIndex = 400L
  )

  lazy val testDbName = DbName("testMapDb")

  lazy val testMapResetResponse = MapResetResponse(
    prevDbName = Some(testDbName),
    prevMapName = Name.some("testMapName")
  )

  lazy val booleanIoNodeDef = BooleanIoNodeDef(IoName("boolDef"), Set(true, false))
  lazy val floatIoNodeDef = FloatIoNodeDef(IoName("floatDef"), min = -1, max = 1)
  lazy val intIoNodeDef = IntIoNodeDef(IoName("intDef"), min = 0, max = 10)
  lazy val listStrIoNodeDef = ListStrIoNodeDef(IoName("listStrDef"), elements = List("a", "b", "c"))

  lazy val testMapInitRequest = MapInitRequest(
    dbName = testDbName,
    name = Name.some("testMapName"),
    description = Description.some("testMapDescription"),
    inputNodes = List(booleanIoNodeDef, floatIoNodeDef),
    outputNodes = List(intIoNodeDef, listStrIoNodeDef)
  )

  lazy val testMapLoadRequest = MapLoadRequest(dbName = testDbName)

  lazy val testMapInfoResponse = MapInfoResponse(
    testDbName,
    testMapInitRequest.name,
    testMapInitRequest.inputNodes.size,
    testMapInitRequest.outputNodes.size,
    numHiddenNodes = 3L
  )

  lazy val testConNodeVal1 = true
  lazy val testConNodeVal2 = "a"

  lazy val booleanIoVar = BooleanIoVariable[IO](booleanIoNodeDef.acceptableValues)
  lazy val floatIoVar = FloatIoVariable[IO](floatIoNodeDef.min, floatIoNodeDef.max)
  lazy val intIoVar = IntIoVariable[IO](intIoNodeDef.min, intIoNodeDef.max)
  lazy val listStrIoVar = ListStrIoVariable[IO](listStrIoNodeDef.elements)

  lazy val booleanIoNode = InputNode(IoName("ioNode1"), booleanIoVar)
  lazy val floatIoNode = InputNode(IoName("ioNode2"), floatIoVar)
  lazy val intIoNode = OutputNode(IoName("ioNode3"), intIoVar)
  lazy val listStrIoNode = OutputNode(IoName("ioNode4"), listStrIoVar)

  lazy val ioNodes = Map(
    booleanIoNode.name -> booleanIoNode,
    floatIoNode.name -> floatIoNode,
    intIoNode.name -> intIoNode,
    listStrIoNode.name -> listStrIoNode
  )

  lazy val testConNodeDef1 = ConcreteNodeDef(
    HnName("conHn1"),
    Description.some("testConNodeDef1"),
    booleanIoNode.name,
    Json.fromBoolean(testConNodeVal1)
  )

  lazy val testConNodeDef2 = ConcreteNodeDef(
    HnName("conHn2"),
    Description.some("testConNodeDef2"),
    listStrIoNode.name,
    Json.fromString(testConNodeVal2)
  )

  lazy val testAbsNodeDef1 = AbstractNodeDef(HnName("absHn3"), Description.some("testAbsNodeDef1"))
  lazy val testAbsNodeDef2 = AbstractNodeDef(HnName("absHn4"), Description.some("testAbsNodeDef2"))

  lazy val testConNodeNew1 = ConcreteNode.New(
    Some(testConNodeDef1.name),
    testConNodeDef1.description,
    testConNodeDef1.ioNodeName,
    booleanIoVar.indexForValue(testConNodeVal1).unsafeRunSync()
  )

  lazy val testConNodeNew2 = ConcreteNode.New(
    Some(testConNodeDef2.name),
    testConNodeDef2.description,
    testConNodeDef2.ioNodeName,
    listStrIoVar.indexForValue(testConNodeVal2).unsafeRunSync()
  )

  lazy val testNewSampleData: NewSampleData = NewSampleData(
    probabilityCount = 10,
    utility = 0.5,
    name = Name.some("sample1"),
    description = Description.some("Sample 1 description"),
    edges = List(NewSampleEdge(testConNodeDef1.name, testAbsNodeDef1.name, EdgeType.THEN))
  )

  lazy val testMapAddSamplesRequest = MapAddSamplesRequest(
    samples = List(
      testNewSampleData,
      NewSampleData(
        probabilityCount = 20,
        utility = 0.8,
        name = Name.some("sample2"),
        description = Description.some("Sample 2 description"),
        edges = List(NewSampleEdge(testConNodeDef2.name, testAbsNodeDef2.name, EdgeType.LINK))
      )
    ),
    hiddenNodes = List(testConNodeDef1, testAbsNodeDef1, testConNodeDef2, testAbsNodeDef2)
  )

  lazy val testMapAddSamplesResponse = MapAddSamplesResponse(
    addedSamples = testMapAddSamplesRequest.samples.zipWithIndex
      .map((data, i) => ShortSampleData(SampleId(i), data.name))
  )

  lazy val testSampleData: SampleData = SampleData(
    id = SampleId(1),
    probabilityCount = testNewSampleData.probabilityCount,
    utility = testNewSampleData.utility,
    name = testNewSampleData.name,
    description = testNewSampleData.description
  )

  lazy val testSample = Sample(data = testSampleData, edges = Set())
  lazy val testDcgSample = DcgSample[IO](data = testSampleData, structure = GraphStructure.empty[IO])

  lazy val testMnIdMap = Map(
    testConNodeDef1.name -> MnId.Con(101L),
    testConNodeDef2.name -> MnId.Con(102L),
    testAbsNodeDef1.name -> MnId.Abs(103L),
    testAbsNodeDef2.name -> MnId.Abs(104L)
  )

  lazy val findHnIdsByNamesRes: Map[HnName, List[MnId]] = Map(
    testConNodeDef1.name -> List(testMnIdMap(testConNodeDef1.name)),
    testAbsNodeDef1.name -> List(testMnIdMap(testAbsNodeDef1.name))
  )

  lazy val newConcreteNodesRes = Map(testMnIdMap(testConNodeDef2.name) -> Some(testConNodeDef2.name))
  lazy val newAbstractNodesRes = Map(testMnIdMap(testAbsNodeDef2.name) -> Some(testAbsNodeDef2.name))

  lazy val expectedSampleNewList = Sample.ListNew(
    testMapAddSamplesRequest.samples.map: sampleData =>
      Sample.New(
        probabilityCount = sampleData.probabilityCount,
        utility = sampleData.utility,
        name = sampleData.name,
        description = sampleData.description,
        edges = sampleData.edges.toSet.map(edge =>
          SampleEdge.New(
            source = testMnIdMap(edge.sourceHnName).asHnId,
            target = testMnIdMap(edge.targetHnName).asHnId,
            edgeType = edge.edgeType
          )
        )
      )
  )

  lazy val testResponse = MapAddSamplesResponse(
    testMapAddSamplesRequest.samples.zipWithIndex.map((data, i) => ShortSampleData(SampleId(i + 1), data.name))
  )

  lazy val tesConcreteDcgNode = DcgNode.Concrete[IO](
    id = MnId.Con(3000005),
    name = Some(HnName("boolOutputNode")),
    description = Description.some("Concrete Dcg Node for bool output"),
    ioNode = booleanIoNode,
    valueIndex = IoIndex(2000001)
  )

  lazy val testAbstractDcgNode = DcgNode.Abstract[IO](
    id = MnId.Abs(3000007),
    name = Some(HnName("abstractNode1")),
    description = Description.some("Abstract Dcg Node 1")
  )

  lazy val testDcgEdge = DcgEdge[IO](
    key = EdgeKey.Link(tesConcreteDcgNode.id, testAbstractDcgNode.id),
    samples =  DcgSamples[IO](Map(testSampleData.id -> Indexies(HnIndex(2000001), HnIndex(3000001)))).unsafeRunSync()
  )

  lazy val testDcgState = MapGraphState[IO](
    ioValues = Map(tesConcreteDcgNode.ioValue -> Set(tesConcreteDcgNode.id)),
    graph = DcgGraph[IO](
      nodes = Map(tesConcreteDcgNode.id -> tesConcreteDcgNode, testAbstractDcgNode.id -> testAbstractDcgNode),
      edges = Map(testDcgEdge.key -> testDcgEdge),
      samples = Map(testSampleData.id -> testSampleData),
      structure = GraphStructure(Set(testDcgEdge.key))
    )
  )

  lazy val testMapInfoState = MapInfoState[IO](
    metadata = MapMetadata(Name.some("Test Map"), Description.some("A map used for testing MapInfoState")),
    inNodes = Map(booleanIoNode.name -> booleanIoNode),
    outNodes = Map(intIoNode.name -> intIoNode)
  )

  lazy val testMapVisualizationMsg = MapVisualizationMsg(
    inNodes = testMapInfoState.inNodes.keySet,
    outNodes = testMapInfoState.outNodes.keySet,
    ioValues =  testDcgState.ioValues.toSet.map((k, v) => (k.name, v.map(_.asHnId))),
    concreteNodes = testDcgState.graph.nodes.keySet.filter(_.isCon).map(_.asHnId),
    abstractNodes =testDcgState.graph.nodes.keySet.filter(_.isAbs).map(_.asHnId),
    edgesMapping =testDcgState.graph.structure.srcMap.toSet.map((s, ts) => (s.asHnId, ts.map(_.id.asHnId)))
  )

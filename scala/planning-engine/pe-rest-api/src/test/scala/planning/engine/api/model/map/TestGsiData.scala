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
| created: 2026-10-06 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.api.model.visualization.gsi.GsiVisualizationMsg
import planning.engine.common.graph.GraphStructure
import planning.engine.common.graph.edges.{Indexies, MeKey}
import planning.engine.common.graph.io.IoValueMap
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnIndex, HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.io.variable.*
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}
import planning.engine.planner.gsi.map.dcg.DcGraph
import planning.engine.planner.gsi.map.dcg.edges.{DcgEdge, DcgSamples}
import planning.engine.planner.gsi.map.dcg.nodes.DcgNode
import planning.engine.planner.gsi.map.dcg.samples.DcgSample
import planning.engine.planner.gsi.map.state.{MapGraphState, MapInfoState}

trait TestGsiData extends TestApiData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val booleanIoVar = BooleanIoVariable[IO](booleanIoNodeDef.acceptableValues)
  lazy val floatIoVar = FloatIoVariable[IO](floatIoNodeDef.min, floatIoNodeDef.max)
  lazy val intIoVar = IntIoVariable[IO](intIoNodeDef.min, intIoNodeDef.max)
  lazy val listStrIoVar = ListStrIoVariable[IO](listStrIoNodeDef.elements)

  lazy val booleanIoNode = InputNode(testConNodeBoolDef.ioNodeName, booleanIoVar)
  lazy val floatIoNode = InputNode(testConNodeFloatDef.ioNodeName, floatIoVar)
  lazy val intIoNode = OutputNode(testConNodeIntDef.ioNodeName, intIoVar)
  lazy val listStrIoNode = OutputNode(testConNodeListStrDef.ioNodeName, listStrIoVar)

  lazy val ioNodes: Map[IoName, IoNode[IO]] = Map(
    booleanIoNode.name -> booleanIoNode,
    floatIoNode.name -> floatIoNode,
    intIoNode.name -> intIoNode,
    listStrIoNode.name -> listStrIoNode,
  )

  lazy val testConNodeNew1 = ConcreteNode.New(
    Some(testConNodeBoolDef.name),
    testConNodeBoolDef.description,
    testConNodeBoolDef.ioNodeName,
    booleanIoVar.indexForValue(testConNodeBoolVal).unsafeRunSync(),
  )

  lazy val testConNodeNew2 = ConcreteNode.New(
    Some(testConNodeListStrDef.name),
    testConNodeListStrDef.description,
    testConNodeListStrDef.ioNodeName,
    listStrIoVar.indexForValue(testConNodeListStrVal).unsafeRunSync(),
  )

  lazy val testSampleData: SampleData = SampleData(
    id = SampleId(1),
    probabilityCount = testNewSampleData.probabilityCount,
    utility = testNewSampleData.utility,
    name = testNewSampleData.name,
    description = testNewSampleData.description,
  )

  lazy val testSample = Sample(data = testSampleData, edges = Set())
  lazy val testDcgSample = new DcgSample[IO](data = testSampleData, structure = GraphStructure.empty[IO])

  lazy val testMnIdMap: Map[HnName, MnId] = Map(
    testConNodeBoolDef.name -> MnId.Con(101L),
    testConNodeListStrDef.name -> MnId.Con(102L),
    testAbsNodeDef1.name -> MnId.Abs(103L),
    testAbsNodeDef2.name -> MnId.Abs(104L),
  )

  lazy val findHnIdsByNamesRes: Map[HnName, List[MnId]] = Map(
    testConNodeBoolDef.name -> List(testMnIdMap(testConNodeBoolDef.name)),
    testAbsNodeDef1.name -> List(testMnIdMap(testAbsNodeDef1.name)),
  )

  lazy val newConcreteNodesRes: Map[MnId, Some[HnName]] =
    Map(testMnIdMap(testConNodeListStrDef.name) -> Some(testConNodeListStrDef.name))

  lazy val newAbstractNodesRes: Map[MnId, Some[HnName]] =
    Map(testMnIdMap(testAbsNodeDef2.name) -> Some(testAbsNodeDef2.name))

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
            edgeType = edge.edgeType,
          ),
        ),
      ),
  )

  lazy val tesConcreteDcgNode = DcgNode.Concrete[IO](
    id = MnId.Con(3000005),
    name = Some(HnName("boolOutputNode")),
    description = Description.some("Concrete Dcg Node for bool output"),
    ioNode = booleanIoNode,
    valueIndex = IoIndex(2000001),
  )

  lazy val testAbstractDcgNode = DcgNode.Abstract[IO](
    id = MnId.Abs(3000007),
    name = Some(HnName("abstractNode1")),
    description = Description.some("Abstract Dcg Node 1"),
  )

  lazy val testDcgEdge = DcgEdge[IO](
    key = MeKey.Link(tesConcreteDcgNode.id, testAbstractDcgNode.id),
    samples = DcgSamples[IO](Map(testSampleData.id -> Indexies(HnIndex(2000001), HnIndex(3000001)))).unsafeRunSync(),
  )

  lazy val testDcgState = new MapGraphState[IO](
    ioValues = new IoValueMap[IO](Map(tesConcreteDcgNode.ioValue -> Set(tesConcreteDcgNode.id))),
    graph = new DcGraph[IO](
      nodes = Map(tesConcreteDcgNode.id -> tesConcreteDcgNode, testAbstractDcgNode.id -> testAbstractDcgNode),
      edges = Map(testDcgEdge.key -> testDcgEdge),
      samples = Map(testSampleData.id -> testSampleData),
      structure = GraphStructure(Set(testDcgEdge.key)),
    ),
  )

  lazy val testMapInfoState = MapInfoState[IO](
    metadata = MapMetadata(Name.some("Test Map"), Description.some("A map used for testing MapInfoState")),
    inNodes = Map(booleanIoNode.name -> booleanIoNode),
    outNodes = Map(intIoNode.name -> intIoNode),
  )

  lazy val testMapVisualizationMsg = GsiVisualizationMsg(
    inNodes = testMapInfoState.inNodes.keySet,
    outNodes = testMapInfoState.outNodes.keySet,
    ioValues = testDcgState.ioValues.valueMap.toSet.map((k, v) => (k.name, v.map(_.asHnId))),
    concreteNodes = testDcgState.graph.nodes.keySet.filter(_.isCon).map(_.asHnId),
    abstractNodes = testDcgState.graph.nodes.keySet.filter(_.isAbs).map(_.asHnId),
    edgesMapping = testDcgState.graph.structure.srcMap.toSet.map((s, ts) => (s.asHnId, ts.map(_.id.asHnId))),
  )

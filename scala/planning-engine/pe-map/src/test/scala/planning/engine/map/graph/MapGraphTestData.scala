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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.graph

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import neotypes.model.types.Node
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.db.DbName
import planning.engine.common.values.node.{HnId, HnIndex, IoIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}
import planning.engine.map.subgraph.NextSampleEdge

trait MapGraphTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testDbName = DbName("test-map-db")

  lazy val testMapConfig = MapConfig(
    initNextHnId = 1L,
    initNextSampleId = 1L,
    initSampleCount = 0L,
    initNextHnIndex = 1L,
    samplesName = "TestSamples"
  )

  lazy val testMetadata = MapMetadata(Some(Name("TestMap")), Some(Description("Test description")))
  lazy val emptyNeo4jNode = Node("test_res_node", Set(), Map())

  lazy val boolInNode = InputNode[IO](Name("boolInputNode"), BooleanIoVariable[IO](Set(true, false)))
  lazy val boolOutNode = OutputNode[IO](Name("boolOutputNode"), BooleanIoVariable[IO](Set(true, false)))

  lazy val boolIoNodes = Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)

  lazy val intInNode = InputNode[IO](Name("intInputNode"), IntIoVariable[IO](0, 10000))
  lazy val intOutNode = OutputNode[IO](Name("intOutputNode"), IntIoVariable[IO](-10000, 10000))

  lazy val allIoNodes = Set(boolInNode, boolOutNode)

  lazy val testHnIndex = HnIndex(1L)

  def makeAbstractNode(id: Int): AbstractNode[IO] = AbstractNode[IO](
    id = HnId(id), 
    name = Some(Name("Abs Test Node")),
    description = Some(Description("Abs Test Node Description"))
  )

  def makeConcreteNode(index: Long, ioNode: IoNode[IO]): ConcreteNode[IO] =
    ConcreteNode[IO](
      id = HnId(123),
      name = Some(Name("Con Test Node")),
      description = Some(Description("Con Test Node Description")),
      ioNode = ioNode,
      valueIndex = IoIndex(index)
    )

  lazy val hiddenNodes: List[AbstractNode[IO]] = (1 to 10).map(i => makeAbstractNode(i)).toList

  lazy val newSample: Sample.New = Sample.New(
    probabilityCount = 10L,
    utility = 0.5,
    name = Some(Name("sample-1")),
    description = Some(Description("This is a test sample")),
    edges = List(SampleEdge.New(HnId(1), HnId(2), EdgeType.THEN), SampleEdge.New(HnId(2), HnId(3), EdgeType.LINK))
  )

  lazy val testSampleData: SampleData = SampleData(
    id = SampleId(1),
    probabilityCount = newSample.probabilityCount,
    utility = newSample.utility,
    name = newSample.name,
    description = newSample.description
  )

  lazy val testSampleEdges: List[SampleEdge] = List(SampleEdge(
    source = SampleEdge.End(HnId(1), HnIndex(10)),
    target = SampleEdge.End(HnId(2), HnIndex(20)),
    edgeType = EdgeType.LINK,
    sampleId = testSampleData.id
  ))

  lazy val testSample: Sample = Sample(data = testSampleData, edges = testSampleEdges)

  lazy val testAbstractNode: AbstractNode[IO] = makeAbstractNode(321)

  lazy val testNextSampleEdge: NextSampleEdge[IO] = NextSampleEdge[IO](
    sampleData = testSampleData,
    currentValue = testHnIndex,
    edgeType = EdgeType.LINK,
    nextValue = testHnIndex,
    nextHn = testAbstractNode
  )

  def makeFourNewSamples(hnId1: HnId, hnId2: HnId, hnId3: HnId): Sample.ListNew = Sample.ListNew.of(
    newSample.copy(name = Some(Name("loop-sample-1")), edges = List(SampleEdge.New(hnId1, hnId1, EdgeType.THEN))),
    newSample.copy(name = Some(Name("one-edge-sample-1")), edges = List(SampleEdge.New(hnId1, hnId2, EdgeType.LINK))),
    newSample.copy(
      name = Some(Name("two-edge-sample-1")),
      edges = List(
        SampleEdge.New(hnId1, hnId2, EdgeType.THEN),
        SampleEdge.New(hnId2, hnId3, EdgeType.LINK)
      )
    ),
    newSample.copy(
      name = Some(Name("three-edge-sample-1")),
      edges = List(
        SampleEdge.New(hnId1, hnId2, EdgeType.THEN),
        SampleEdge.New(hnId2, hnId3, EdgeType.LINK),
        SampleEdge.New(hnId3, hnId1, EdgeType.THEN)
      )
    )
  )

  def makeTwoNoNameNewSamples(hnId1: HnId, hnId2: HnId): Sample.ListNew = Sample.ListNew.of(
    newSample.copy(name = None, edges = List(SampleEdge.New(hnId1, hnId2, EdgeType.THEN))),
    newSample.copy(name = None, edges = List(SampleEdge.New(hnId2, hnId1, EdgeType.THEN)))
  )

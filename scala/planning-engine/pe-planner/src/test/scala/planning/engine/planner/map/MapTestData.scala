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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.scalatest.matchers.must.Matchers
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.InputNode
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.map.samples.sample.{SampleData, SampleEdge}
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.{AbstractDcgNode, ConcreteDcgNode}

trait MapTestData:
  self: Matchers =>
    private implicit lazy val ioRuntime: IORuntime = IORuntime.global
  
    lazy val testBoolInNode = InputNode[IO](IoName("boolInputNode"), BooleanIoVariable[IO](Set(true, false)))
    lazy val testIntInNode = InputNode[IO](IoName("intInputNode"), IntIoVariable[IO](0, 10000))
  
    lazy val testSampleIndexies: SampleIndexies = SampleIndexies(
      sampleId = SampleId(1000001),
      sourceIndex = HnIndex(2000001),
      targetIndex = HnIndex(2000002)
    )
  
    lazy val testHiddenEdge: HiddenEdge = HiddenEdge(
      edgeType = EdgeType.LINK,
      sourceId = HnId(3000001),
      targetId = HnId(3000002),
      samples = List(testSampleIndexies)
    )
  
    def makeAbstractNode(id: HnId = HnId(3000003)): AbstractNode[IO] = AbstractNode[IO](
      id = id,
      name = HnName.some(s"Abs Node $id"),
      description = Description.some(s"Test Abstract Node, ID $id")
    )
  
    def makeConcreteNode(id: HnId = HnId(3000004), index: IoIndex = IoIndex(101)): ConcreteNode[IO] = ConcreteNode[IO](
      id = id,
      name = HnName.some(s"Con Node $id"),
      description = Description.some(s"Test Concrete Node, ID $id"),
      ioNode = testBoolInNode,
      valueIndex = index
    )
  
    def makeSampleData(id: SampleId = SampleId(1000002)): SampleData = SampleData(
      id = id,
      probabilityCount = 10L,
      utility = 5.0,
      name = Name.some(s"Sample Data $id"),
      description = Description.some(s"Test Sample Data, ID $id")
    )
  
    lazy val testMapSubGraph: MapSubGraph[IO] = MapSubGraph[IO](
      concreteNodes = List(makeConcreteNode(HnId(3000011))),
      abstractNodes = List(makeAbstractNode(HnId(3000012))),
      edges = List(testHiddenEdge.copy(sourceId = HnId(3000011), targetId = HnId(3000012))),
      loadedSamples = List(makeSampleData(testSampleIndexies.sampleId)),
      skippedSamples = List(SampleId(1000101))
    )
  
    def makeConcreteDcgNode(
        id: HnId = HnId(3000005),
        valueIndex: IoIndex = IoIndex(102)
    ): ConcreteDcgNode[IO] = ConcreteDcgNode[IO](
      id = id,
      name = HnName.some(s"Con DCG Node $id"),
      ioNode = testBoolInNode,
      valueIndex = valueIndex
    )
  
    def makeAbstractDcgNode(id: HnId = HnId(3000006)): AbstractDcgNode[IO] = AbstractDcgNode[IO](
      id = id,
      name = HnName.some(s"Abs DCG Node $id")
    )
  
    lazy val testDcgEdgeKey: DcgEdge.Key = DcgEdge.Key(
      edgeType = EdgeType.LINK,
      sourceId = HnId(3000007),
      targetId = HnId(3000008)
    )
  
    def makeDcgEdge(
        sampleId: SampleId = testHiddenEdge.samples.head.sampleId,
        sourceId: HnId = testDcgEdgeKey.sourceId,
        targetId: HnId = testDcgEdgeKey.targetId,
        indexies: Map[HnId, HnIndex] = Map(
          testDcgEdgeKey.sourceId -> HnIndex(2000003),
          testDcgEdgeKey.targetId -> HnIndex(2000004))
    ): DcgEdge[IO] = DcgEdge[IO](
      key = testDcgEdgeKey.copy(sourceId = sourceId, targetId = targetId),
      samples = Map(sampleId -> DcgEdge.Indexies(
        sourceIndex = indexies.getOrElse(sourceId, fail(s"Missing source HnIndex for $sourceId")),
        targetIndex = indexies.getOrElse(targetId, fail(s"Missing target HnIndex for $targetId"))
      ))
    )

    lazy val testDcgEdge: DcgEdge[IO] = makeDcgEdge()
  
    def makeIoValue(
        name: String = testBoolInNode.name.value,
        index: Long = 2000003L
    ): IoValue = IoValue(
      name = IoName(name),
      index = IoIndex(index)
    )
  
    lazy val testSampleEdge: SampleEdge = SampleEdge(
      sampleId = SampleId(1000003),
      source = SampleEdge.End(HnId(3000009), HnIndex(2000005)),
      target = SampleEdge.End(HnId(3000010), HnIndex(2000006)),
      edgeType = EdgeType.LINK
    )
  
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
| created: 2025-12-22 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.unsafe.IORuntime
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}

trait MapSampleTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  def makeNewSampleData(
      source: HnId = HnId(10001),
      target: HnId = HnId(10002),
      edgeType: EdgeType = EdgeType.LINK,
      name: Option[Name] = Name.some("New Sample Data")
  ): Sample.New = Sample.New(
    probabilityCount = 20L,
    utility = 10.0,
    name = name,
    description = Description.some("Test New Sample Data"),
    edges = Set(SampleEdge.New(source, target, edgeType))
  )

  def makeSampleData(id: SampleId = SampleId(1000002)): SampleData = SampleData(
    id = id,
    probabilityCount = 10L,
    utility = 5.0,
    name = Name.some(s"Sample Data $id"),
    description = Description.some(s"Test Sample Data, ID $id")
  )

  def makeSampleEdge(
      sampleId: SampleId = SampleId(1000004),
      sourceHnId: HnId = HnId(3000009),
      targetHnIdt: HnId = HnId(3000010),
      edgeType: EdgeType = EdgeType.LINK
  ): SampleEdge = SampleEdge(
    sampleId = sampleId,
    source = SampleEdge.End(sourceHnId, HnIndex(sampleId.value + sourceHnId.value)),
    target = SampleEdge.End(targetHnIdt, HnIndex(sampleId.value + targetHnIdt.value)),
    edgeType = edgeType
  )

  lazy val testSampleEdge: SampleEdge = makeSampleEdge()

  def makeSample(
      sampleId: SampleId = SampleId(10000045),
      sourceHnId: HnId = HnId(30000011),
      targetHnIdt: HnId = HnId(3000012),
      edgeType: EdgeType = EdgeType.LINK
  ): Sample = Sample(
    data = makeSampleData(sampleId),
    edges = Set(makeSampleEdge(sampleId, sourceHnId, targetHnIdt, edgeType))
  )

  def makeHiddenEdges(samples: List[Sample]): List[HiddenEdge] = samples
    .flatMap(s => s.edges.map(e => (e.edgeType, e.source, e.target, s.data.id)))
    .groupBy(r => (r._1, r._2.hnId, r._3.hnId)).view
    .map((k, r) =>
      HiddenEdge(
        edgeType = k._1,
        sourceId = k._2,
        targetId = k._3,
        samples = r
          .map(t => HiddenEdge.SampleIndexies(sampleId = t._4, sourceIndex = t._2.value, targetIndex = t._3.value))
      )
    ).toList

  def makeNewSampleData(data: SampleData, edges: Set[EdgeKey]): Sample.New = Sample.New(
    probabilityCount = data.probabilityCount,
    utility = data.utility,
    name = data.name,
    description = data.description,
    edges = edges.map(e => SampleEdge.New(e.src.asHnId, e.trg.asHnId, e.asEdgeType))
  )

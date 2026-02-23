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
| created: 2026-02-18 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.edge.{EdgeKey, IndexMap}
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.common.graph.GraphStructure

trait DcgSampleTestData extends DcgNodeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val sampleId1 = SampleId(1001)
  lazy val sampleId2 = SampleId(1002)
  lazy val sampleId3 = SampleId(1003)
  lazy val sampleId4 = SampleId(1004)
  lazy val sampleId5 = SampleId(1005)
  lazy val simpleSampleId = SampleId(2003)

  def makeDcgSampleData(id: SampleId): SampleData = SampleData(
    id = id,
    probabilityCount = 10L,
    utility = 5.0,
    name = Name.some(s"DCG Sample Data $id"),
    description = Description.some(s"Test DCG Sample Data, ID $id")
  )

  def makeDcgSample(id: SampleId)(keys: EdgeKey*): DcgSample[IO] = new DcgSample[IO](
    data = makeDcgSampleData(id),
    structure = GraphStructure[IO](keys.toSet)
  )

  def makeDcgIndexMap(sId: SampleId, mnIds: Set[MnId]): IndexMap =
    IndexMap(mnIds.map(id => id -> HnIndex((sId.value * 10000) + (id.value * 100))).toMap)

  def makeDcgSampleAdd(id: SampleId)(keys: EdgeKey*): DcgSample.Add[IO] = DcgSample.Add[IO](
    sample = makeDcgSample(id)(keys*),
    indexMap = makeDcgIndexMap(id, keys.flatMap(k => Set(k.src, k.trg)).toSet)
  )

  lazy val simpleSampleKeys = Set(EdgeKey.Link(mnId1, mnId3), EdgeKey.Then(mnId3, mnId4))
  lazy val simpleSample = makeDcgSample(simpleSampleId)(simpleSampleKeys.toSeq*)
  lazy val simpleSampleAdd = makeDcgSampleAdd(simpleSampleId)(simpleSampleKeys.toSeq*)

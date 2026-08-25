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
| created: 04.07.2026 |||||||||||*/

package planning.engine.planner.mpi.test.data

import cats.effect.unsafe.IORuntime
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.common.data.samples.Sample

trait MapEdgeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  def makePropVals(i: Int): Sample.Props = Sample.Props(i + 100, i / 100)

  lazy val props1: Map[SampleId, Sample.Props] = List(1, 2, 3).map(i => SampleId(i) -> makePropVals(i)).toMap
  lazy val props2: Map[SampleId, Sample.Props] = List(4, 5).map(i => SampleId(i) -> makePropVals(i)).toMap

//
//  def makeSampleIndex(i: Int): (SampleId, Indexies) =
//    SampleId(i) -> Indexies(src = HnIndex(i + 1000), trg = HnIndex(i + 2000))
//
//  def makeEdgeData(ses: Int*): EdgeData = EdgeData(ses.map(makeSampleIndex).toMap)
//
//  lazy val edgeData1: EdgeData = makeEdgeData(1, 2, 3) // Sample IDs: 1, 2, 3
//  lazy val edgeData2: EdgeData = makeEdgeData(4, 5) // Sample IDs: 4, 5

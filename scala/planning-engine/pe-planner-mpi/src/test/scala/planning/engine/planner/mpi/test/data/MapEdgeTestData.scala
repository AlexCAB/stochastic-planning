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

import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.common.data.samples.Sample

trait MapEdgeTestData:
  self: UnitSpecWithData =>

  trait WithMapEdge:
    def makePropVals(i: Int): Sample.Props = Sample.Props(i + 100, i / 100)

    lazy val props1: Map[SampleId, Sample.Props] = List(1, 2, 3).map(i => SampleId(i) -> makePropVals(i)).toMap
    lazy val props2: Map[SampleId, Sample.Props] = List(4, 5).map(i => SampleId(i) -> makePropVals(i)).toMap

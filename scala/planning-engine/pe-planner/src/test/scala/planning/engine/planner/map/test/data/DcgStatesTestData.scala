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
| created: 2026-02-20 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.io.IoValue
import planning.engine.common.graph.io.IoValueMap

trait DcgStatesTestData extends DcgGraphTestData with DcgSampleTestData:
  lazy val emptyDcgState: MapGraphState[IO] = MapGraphState.empty[IO]

  lazy val initDcgState: MapGraphState[IO] = new MapGraphState(
    ioValues = new IoValueMap[IO](conNodes.groupBy(_.ioValue).map((io, ns) => io -> ns.map(_.id).toSet)),
    graph = graphWithEdges
  )

  lazy val initMapInfoState = MapInfoState[IO](
    metadata = testMetadata,
    inNodes = testInNodes.map(n => n.name -> n).toMap,
    outNodes = testOutNodes.map(n => n.name -> n).toMap
  )

  def makeIoValueMap(values: (IoValue, Set[MnId.Con])*): IoValueMap[IO] = new IoValueMap[IO](values.toMap)

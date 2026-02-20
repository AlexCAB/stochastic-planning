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

trait DcgStatesTestData extends DcgGraphTestData with DcgSampleTestData:
  lazy val emptyDcgState: MapGraphState[IO] = MapGraphState.empty[IO]

  lazy val initDcgState: MapGraphState[IO] = new MapGraphState(
    ioValues = conNodes.groupBy(_.ioValue).map((io , ns) => io -> ns.map(_.id).toSet).toMap,
    graph = graphWithEdges
  )

  lazy val initMapInfoState = MapInfoState[IO](
    metadata = testMetadata,
    inNodes = testInNodes.map(n => n.name -> n).toMap,
    outNodes = testOutNodes.map(n => n.name -> n).toMap
  )

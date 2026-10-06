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
| created: 2025-12-31 |||||||||||*/

package planning.engine.api.service.visualization.gsi

import cats.effect.IO
import cats.effect.cps.*
import fs2.Stream
import planning.engine.api.config.parts.VisualizationServiceConf
import planning.engine.api.model.map.TestGsiData
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.common.UnitSpecWithData

import scala.concurrent.duration.DurationInt

class VisualizationGsiServiceSpec extends UnitSpecWithData with TestGsiData:

  private class CaseData extends Case:
    val config = VisualizationServiceConf(mapEnabled = true)
    val service = VisualizationGsiService.init[IO](config).unsafeRunSync()

  "VisualizationGsiService.mapSendWs" should:
    "provide map visualization messages when enabled" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        (IO.sleep(1.second) *> data.service.stateUpdated(testMapInfoState, testDcgState)).start.await

        val messages: List[MapVisualizationMsg] = data.service.mapSendWs
          .take(1).compile.toList.logValue(tn, "received")
          .await

        messages.size mustBe 1
        messages.head mustBe testMapVisualizationMsg

  "VisualizationGsiService.mapReceiveWs" should:
    "log received ping messages" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.service.mapReceiveWs(Stream.emit("test-ping-msg")).compile.drain.await
        succeed

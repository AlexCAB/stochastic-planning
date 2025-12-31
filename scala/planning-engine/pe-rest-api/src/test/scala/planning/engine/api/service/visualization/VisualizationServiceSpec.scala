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

package planning.engine.api.service.visualization

import cats.effect.IO
import cats.effect.cps.*
import fs2.Stream
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.api.config.VisualizationServiceConf
import planning.engine.api.model.map.TestApiData
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.common.UnitSpecWithData

import scala.concurrent.duration.DurationInt

class VisualizationServiceSpec extends UnitSpecWithData with AsyncMockFactory with TestApiData:

  private class CaseData extends Case:
    val config = VisualizationServiceConf(mapEnabled = true)
    val service = VisualizationService.init[IO](config).unsafeRunSync()

  "VisualizationService.mapSendWs" should:
    "provide map visualization messages when enabled" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        (IO.sleep(1.second) *> data.service.stateUpdated(testMapInfoState, testDcgState)).start.await

        val messages: List[MapVisualizationMsg] = data.service.mapSendWs
          .take(1).compile.toList.logValue(tn, "received")
          .await

        messages.size mustBe 1
        messages.head mustBe testMapVisualizationMsg

  "VisualizationService.mapReceiveWs" should:
    "log received ping messages" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.service.mapReceiveWs(Stream.emit("test-ping-msg")).compile.drain.await
        succeed

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
| created: 2025-07-01 |||||||||||*/

package planning.engine.map.graph

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.properties.PROP
import planning.engine.common.properties.*

class MapConfigSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val rawConfig: String =
      """
        |knowledge-graph {
        |  init-next-hn-id = 100,
        |  init-next-sample-id = 1000,
        |  init-sample-count = 5000,
        |  init-next-hn-index = 100,
        |  samples-name = "test-samples"
        |}
      """.stripMargin

    val parsedConfig: MapConfig = MapConfig(
      initNextHnId = 100,
      initNextSampleId = 1000,
      initSampleCount = 5000,
      initNextHnIndex = 100,
      samplesName = "test-samples"
    )

  "formConfig" should:
    "load configuration successfully" in newCase[CaseData]: (_, data) =>
      MapConfig
        .formConfig[IO](ConfigFactory.parseString(data.rawConfig).getConfig("knowledge-graph"))
        .asserting(_ mustEqual data.parsedConfig)

  "toRootParams" should:
    "make root parameters" in newCase[CaseData]: (_, data) =>
      data.parsedConfig.toRootParams[IO].asserting(_ mustEqual Map(
        PROP.NEXT_HN_ID -> data.parsedConfig.initNextHnId.toDbParam
      ))

  "toSamplesParams" should:
    "make samples parameters" in newCase[CaseData]: (_, data) =>
      data.parsedConfig.toSamplesParams[IO].asserting(_ mustEqual Map(
        PROP.NEXT_SAMPLES_ID -> data.parsedConfig.initNextSampleId.toDbParam,
        PROP.SAMPLES_COUNT -> data.parsedConfig.initSampleCount.toDbParam,
        PROP.NAME -> data.parsedConfig.samplesName.toDbParam
      ))

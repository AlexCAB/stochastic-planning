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
| created: 2025-07-12 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import cats.effect.cps.*
import io.circe.Json
import planning.engine.api.model.map.payload.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.{Description, Name}

class MapAddSamplesRequestSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testEdge = NewSampleEdge(
      sourceHnName = HnName("hn1"),
      targetHnName = HnName("hn2"),
      edgeType = EdgeType.THEN,
    )

    lazy val testConcreteNodeDef = ConcreteNodeDef(
      testEdge.sourceHnName,
      Description.some("testConcreteNodeDef"),
      IoName("ioNode1"),
      Json.fromLong(1234L),
    )

    lazy val testAbstractNodeDef = AbstractNodeDef(testEdge.targetHnName, Description.some("testAbstractNodeDef"))

    lazy val testNewSampleData: NewSampleData = NewSampleData(
      probabilityCount = 10,
      utility = 0.5,
      name = Name.some("sample1"),
      description = Description.some("Sample 1 description"),
      edges = List(testEdge),
    )

    lazy val testRequest: MapAddSamplesRequest = MapAddSamplesRequest(
      samples = List(testNewSampleData),
      hiddenNodes = List(testConcreteNodeDef, testAbstractNodeDef),
    )

  "MapAddSamplesRequest.hnNames" should:
    "list all hidden node names" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.testRequest.hnNames mustEqual List(data.testConcreteNodeDef.name, data.testAbstractNodeDef.name)

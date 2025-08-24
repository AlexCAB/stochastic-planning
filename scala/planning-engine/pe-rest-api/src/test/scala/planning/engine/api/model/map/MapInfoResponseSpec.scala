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
| created: 2025-04-28 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.Name
import planning.engine.map.data.MapMetadata
import planning.engine.map.{MapGraphLake, MapGraphTestData}

class MapInfoResponseSpec extends UnitSpecWithData with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    val mockKnowledgeGraph = mock[MapGraphLake[IO]]
    val validMetadata = MapMetadata(Name.some("TestMap"), None)
    val validInputNodes = List(boolInNode)
    val validOutputNodes = List(boolOutNode)
    val ioNodes = validInputNodes.map(n => n.name -> n).++(validOutputNodes.map(n => n.name -> n)).toMap
    val testNumOfHiddenNodes = 5L

    (() => mockKnowledgeGraph.countHiddenNodes).expects().returns(IO.pure(testNumOfHiddenNodes)).once()
    (() => mockKnowledgeGraph.metadata).expects().returns(validMetadata).once()
    (() => mockKnowledgeGraph.ioNodes).expects().returns(ioNodes).twice()

  "MapInfoResponse.fromKnowledgeGraph(...)" should:
    "create MapInfoResponse with correct values from a valid knowledge graph" in newCase[CaseData]: (tn, data) =>
      MapInfoResponse.fromMapGraph(testDbName, data.mockKnowledgeGraph)
        .logValue(tn)
        .asserting(_ mustEqual MapInfoResponse(
          testDbName,
          data.validMetadata.name,
          data.validInputNodes.size,
          data.validOutputNodes.size,
          data.testNumOfHiddenNodes
        ))

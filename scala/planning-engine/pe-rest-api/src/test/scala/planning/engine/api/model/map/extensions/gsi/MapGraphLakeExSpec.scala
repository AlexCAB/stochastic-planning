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

package planning.engine.api.model.map.extensions.gsi

import cats.effect.IO
import cats.effect.cps.*
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.api.model.map.MapInfoResponse
import planning.engine.api.model.map.extensions.gsi.MapGraphLakeEx.toResponse
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.Name
import planning.engine.map.data.MapMetadata
import planning.engine.map.{MapGraphLake, MapGraphTestData}

class MapGraphLakeExSpec extends UnitSpecWithData with AsyncIdiomaticMockito with MapGraphTestData:

  private class CaseData extends Case:
    val mockKnowledgeGraph: MapGraphLake[IO] = mock[MapGraphLake[IO]]
    val validMetadata = MapMetadata(Name.some("TestMap"), None)
    val validInputNodes = List(boolInNode)
    val validOutputNodes = List(boolOutNode)
    val ioNodes = validInputNodes.map(n => n.name -> n).++(validOutputNodes.map(n => n.name -> n)).toMap
    val testNumOfHiddenNodes = 5L

    mockKnowledgeGraph.countHiddenNodes returns IO.pure(testNumOfHiddenNodes)
    mockKnowledgeGraph.metadata returns validMetadata
    mockKnowledgeGraph.ioNodes returns ioNodes

  "MapInfoResponse.toResponse(...)" should:
    "create MapInfoResponse with correct values from a valid knowledge graph" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val response = data.mockKnowledgeGraph.toResponse(testDbName).logValue(tn).await

        data.mockKnowledgeGraph.countHiddenNodes was called
        data.mockKnowledgeGraph.metadata was called
        data.mockKnowledgeGraph.ioNodes wasCalled twice
        response mustEqual MapInfoResponse(
          testDbName,
          data.validMetadata.name,
          data.validInputNodes.size,
          data.validOutputNodes.size,
          data.testNumOfHiddenNodes,
        )

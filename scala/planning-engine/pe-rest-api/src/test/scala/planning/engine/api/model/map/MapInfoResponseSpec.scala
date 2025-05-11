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
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.{OpDescription, OpName}
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.knowledge.graph.{KnowledgeGraphLake, Metadata}

class MapInfoResponseSpec extends UnitSpecIO with AsyncMockFactory:

  private class CaseData extends Case:
    val mockKnowledgeGraph = mock[KnowledgeGraphLake[IO]]
    val validMetadata = Metadata(OpName.fromString("TestMap"), OpDescription.empty)
    val validInputNodes = Vector(mock[InputNode[IO]])
    val validOutputNodes = Vector(mock[OutputNode[IO]])
    val testNumOfHiddenNodes = 5L

    (() => mockKnowledgeGraph.countHiddenNodes).expects().returns(IO.pure(testNumOfHiddenNodes)).once()
    (() => mockKnowledgeGraph.metadata).expects().returns(validMetadata).once()
    (() => mockKnowledgeGraph.inputNodes).expects().returns(validInputNodes).once()
    (() => mockKnowledgeGraph.outputNodes).expects().returns(validOutputNodes).once()

  "fromKnowledgeGraph" should:
    "create MapInfoResponse with correct values from a valid knowledge graph" in newCase[CaseData]: data =>
      MapInfoResponse.fromKnowledgeGraph(data.mockKnowledgeGraph)
        .logValue
        .asserting(_ mustEqual MapInfoResponse(
          data.validMetadata.name.value,
          data.validInputNodes.size,
          data.validOutputNodes.size,
          data.testNumOfHiddenNodes
        ))

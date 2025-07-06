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
| created: 2025-07-04 |||||||||||*/

package planning.engine.map.samples.sample

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.db.Neo4j
import neotypes.model.types.{Relationship, Value}

class SampleEdgeSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val sampleId = SampleId(100)

    val newEdge = SampleEdge.New(
      source = HnId(1),
      target = HnId(2),
      edgeType = EdgeType.THEN
    )

    val expectedEdge = SampleEdge(
      sourceValue = HnIndex(10),
      targetValue = HnIndex(20),
      edgeType = newEdge.edgeType,
      sampleId = sampleId
    )

    val rawEdge = Relationship(
      elementId = "element-1",
      relationshipType = Neo4j.THEN_LABEL,
      startNodeId = "start-1",
      endNodeId = "end-2",
      properties = Map(
        sampleId.toPropName -> Value.ListValue(List(
          Value.Integer(expectedEdge.sourceValue.value),
          Value.Integer(expectedEdge.targetValue.value)
        ))
      )
    )

  "SampleEdge.toQueryParams(...)" should:
    "return correct query parameters when indices are valid" in newCase[CaseData]: (_, data) =>
      val indices = Map(HnId(1) -> HnIndex(10), HnId(2) -> HnIndex(20))
      data.newEdge.toQueryParams[IO](data.sampleId, indices)
        .asserting(_ mustEqual ("s100", List(10L, 20L)))

    "raise an error when source index is missing" in newCase[CaseData]: (_, data) =>
      val indices = Map(HnId(2) -> HnIndex(20)) // Missing source index
      data.newEdge.toQueryParams[IO](data.sampleId, indices).assertThrows[AssertionError]

  "SampleEdge.fromEdgeBySampleId(...)" should:
    "return a valid SampleEdge when edge properties are correctly mapped" in newCase[CaseData]: (_, data) =>
      SampleEdge
        .fromEdgeBySampleId[IO](data.rawEdge, data.sampleId)
        .asserting(_ mustEqual data.expectedEdge)

    "raise an error when edge properties list is empty" in newCase[CaseData]: (_, data) =>
      SampleEdge
        .fromEdgeBySampleId[IO](
          data.rawEdge.copy(properties =
            Map(data.sampleId.toPropName -> Value.ListValue(List(
              Value.Integer(10L),
              Value.Integer(20L),
              Value.Integer(30L)
            )))
          ),
          data.sampleId
        ).assertThrows[AssertionError]

    "raise an error when edge properties list has more than two values" in newCase[CaseData]: (_, data) =>
      SampleEdge
        .fromEdgeBySampleId[IO](
          data.rawEdge.copy(properties =
            Map(
              "s200" -> Value.ListValue(List(
                Value.Integer(data.expectedEdge.sourceValue.value),
                Value.Integer(data.expectedEdge.targetValue.value)
              ))
            )
          ),
          data.sampleId
        ).assertThrows[AssertionError]

  "SampleEdge.fromEdge(...)" should:
    "return set of SampleEdge's" in newCase[CaseData]: (tn, data) =>
      SampleEdge
        .fromEdge[IO](data.rawEdge).logValue(tn, "fromEdge")
        .asserting(_ mustEqual Set(data.expectedEdge))

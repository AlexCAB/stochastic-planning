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
    val sampleId1 = SampleId(100)
    val sampleId2 = SampleId(200)

    val newEdge = SampleEdge.New(
      source = HnId(1),
      target = HnId(2),
      edgeType = EdgeType.THEN
    )

    val expectedEdge1 = SampleEdge(
      source = SampleEdge.End(newEdge.source, HnIndex(10)),
      target = SampleEdge.End(newEdge.target, HnIndex(20)),
      edgeType = newEdge.edgeType,
      sampleId = sampleId1
    )

    val expectedEdge2 = SampleEdge(
      source = SampleEdge.End(newEdge.source, HnIndex(30)),
      target = SampleEdge.End(newEdge.target, HnIndex(40)),
      edgeType = newEdge.edgeType,
      sampleId = sampleId2
    )

    val rawEdge1 = Relationship(
      elementId = "element-1",
      relationshipType = Neo4j.THEN_LABEL,
      startNodeId = "start-1",
      endNodeId = "end-1",
      properties = Map(
        sampleId1.toPropName -> Value.ListValue(List(
          Value.Integer(expectedEdge1.source.value.value),
          Value.Integer(expectedEdge1.target.value.value)
        )),
        sampleId2.toPropName -> Value.ListValue(List(
          Value.Integer(expectedEdge2.source.value.value),
          Value.Integer(expectedEdge2.target.value.value)
        ))
      )
    )

    val rawEdge2 = Relationship(
      elementId = "element-2",
      relationshipType = Neo4j.THEN_LABEL,
      startNodeId = "start-2",
      endNodeId = "end-2",
      properties = Map(
        sampleId2.toPropName -> Value.ListValue(List(
          Value.Integer(456),
          Value.Integer(654)
        ))
      )
    )

  "SampleEdge.toQueryParams(...)" should:
    "return correct query parameters when indices are valid" in newCase[CaseData]: (_, data) =>
      val indices = Map(HnId(1) -> HnIndex(10), HnId(2) -> HnIndex(20))
      data.newEdge.toQueryParams[IO](data.sampleId1, indices)
        .asserting(_ mustEqual ("s100", List(10L, 20L)))

    "raise an error when source index is missing" in newCase[CaseData]: (_, data) =>
      val indices = Map(HnId(2) -> HnIndex(20)) // Missing source index
      data.newEdge.toQueryParams[IO](data.sampleId1, indices).assertThrows[AssertionError]

  "SampleEdge.fromEdgeBySampleId(...)" should:
    "return a valid SampleEdge when edge properties are correctly mapped" in newCase[CaseData]: (_, data) =>
      SampleEdge
        .fromEdgesBySampleId[IO](
          List(
            (data.newEdge.source, data.rawEdge1, data.newEdge.target),
            (data.newEdge.source, data.rawEdge2, data.newEdge.target)
          ),
          data.sampleId1
        )
        .asserting(_ mustEqual List(data.expectedEdge1))

    "raise an error when edge properties list has more than two values" in newCase[CaseData]: (_, data) =>
      val invalidEdge = data.rawEdge1.copy(properties =
        Map(data.sampleId1.toPropName -> Value.ListValue(List(
          Value.Integer(10L),
          Value.Integer(20L),
          Value.Integer(30L)
        )))
      )

      SampleEdge
        .fromEdgesBySampleId[IO](List((data.newEdge.source, invalidEdge, data.newEdge.target)), data.sampleId1)
        .assertThrows[AssertionError]

  "SampleEdge.fromEdge(...)" should:
    "return set of SampleEdge's" in newCase[CaseData]: (tn, data) =>
      SampleEdge
        .fromEdge[IO](data.newEdge.source, data.newEdge.target, data.rawEdge1).logValue(tn, "fromEdge")
        .asserting(_ mustEqual List(data.expectedEdge1, data.expectedEdge2))

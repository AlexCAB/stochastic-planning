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

package planning.engine.map.samples

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.common.values.sample.SampleId
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.properties.*

class SampleSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val sampleId = SampleId(100)

    val newSample = Sample.New(
      probabilityCount = 10,
      utility = 0.5,
      name = Some(Name("SampleName")),
      description = Some(Description("SampleDescription")),
      edges = Set(SampleEdge.New(HnId(1), HnId(2), EdgeType.LINK), SampleEdge.New(HnId(2), HnId(3), EdgeType.LINK))
    )

  "New.hnIds" should:
    "distinct list ofHnId" in newCase[CaseData]: (_, data) =>
      data.newSample.hnIds.pure[IO].asserting(_ mustEqual Set(HnId(1), HnId(2), HnId(3)))

  "New.validationName" should:
    "return validation name" in newCase[CaseData]: (tn, data) =>
      data.newSample.validationName.pure[IO].logValue(tn, "validationName")
        .asserting(_ mustEqual "Sample(name=SampleName, probabilityCount=10, utility=0.5)")

  "New.validationErrors" should:
    "return empty list for valid data" in newCase[CaseData]: (_, data) =>
      data.newSample.validationErrors.pure[IO].asserting(_ mustEqual List())

    "return list errors for invalid data" in newCase[CaseData]: (tn, data) =>
      val invalidSample = data.newSample.copy(
        probabilityCount = 0, // Invalid count
        name = Some(Name("")), // Empty name
        description = Some(Description("")), // Empty description
        edges = Set() // No edges
      )

      invalidSample.validationErrors.pure[IO].logValue(tn, "validationErrors").asserting(_.size mustEqual 4)

  "New.findHnIndexies" should:
    "return map of HnIndex" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val indicesMap = Map(
          HnId(1) -> List(HnIndex(10), HnIndex(11)),
          HnId(2) -> List(HnIndex(20), HnIndex(21)),
          HnId(3) -> List(HnIndex(30))
        )

        val (indices, newIndicesMap): (Map[HnId, List[HnIndex]], Map[HnId, HnIndex]) = data
          .newSample.findHnIndexies[IO](indicesMap).logValue(tn, "validationName").await

        indices mustEqual Map(
          HnId(1) -> List(HnIndex(11)),
          HnId(2) -> List(HnIndex(21)),
          HnId(3) -> List()
        )

        newIndicesMap mustEqual Map(
          HnId(1) -> HnIndex(10),
          HnId(2) -> HnIndex(20),
          HnId(3) -> HnIndex(30)
        )

    "fail if in indices not found for HnId" in newCase[CaseData]: (tn, data) =>
      val indicesMap = Map(HnId(1) -> List(HnIndex(10)), HnId(2) -> List(HnIndex(20)))
      data.newSample.findHnIndexies[IO](indicesMap).logValue(tn, "validationName").assertThrows[AssertionError]

    "fail if in indices not enought HnIndex" in newCase[CaseData]: (tn, data) =>
      val indicesMap = Map(HnId(1) -> List(HnIndex(10)), HnId(2) -> List(HnIndex(20)), HnId(3) -> List())
      data.newSample.findHnIndexies[IO](indicesMap).logValue(tn, "validationName").assertThrows[AssertionError]

  "New.toQueryParams" should:
    "return correct parameters when all fields are valid" in newCase[CaseData]: (tn, data) =>
      data.newSample.toQueryParams[IO](data.sampleId).logValue(tn, "toQueryParams")
        .asserting(_ mustEqual Map(
          PROP.SAMPLE_ID -> data.sampleId.toDbParam,
          PROP.PROBABILITY_COUNT -> data.newSample.probabilityCount.toDbParam,
          PROP.UTILITY -> data.newSample.utility.toDbParam,
          PROP.NAME -> data.newSample.name.get.toDbParam,
          PROP.DESCRIPTION -> data.newSample.description.get.toDbParam
        ))

  "New.toQueryParams" should:
    "return correct aggrigate values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val newSample2 = data.newSample.copy(edges = Set(SampleEdge.New(HnId(3), HnId(4), EdgeType.LINK)))
        val newSamples = Sample.ListNew(Set(data.newSample, newSample2))

        newSamples.allEdges mustEqual Set(
          SampleEdge.New(HnId(1), HnId(2), EdgeType.LINK),
          SampleEdge.New(HnId(2), HnId(3), EdgeType.LINK),
          SampleEdge.New(HnId(3), HnId(4), EdgeType.LINK)
        )

        newSamples.allHnIds mustEqual Set(HnId(1), HnId(2), HnId(3), HnId(4))
        newSamples.numHnIndexPerHn mustEqual Map(HnId(1) -> 1, HnId(2) -> 1, HnId(3) -> 2, HnId(4) -> 1)

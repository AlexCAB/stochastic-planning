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
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.common.values.sample.SampleId
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.properties.*
import planning.engine.map.samples.sample.SampleEdge.End

class SampleSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val sampleId = SampleId(100)

    lazy val newSample = Sample.New(
      probabilityCount = 10,
      utility = 0.5,
      name = Name.some("SampleName"),
      description = Description.some("SampleDescription"),
      edges = List(SampleEdge.New(HnId(1), HnId(2), EdgeType.LINK), SampleEdge.New(HnId(2), HnId(3), EdgeType.LINK))
    )

    lazy val hnIndexMap = Map(
      HnId(1) -> HnIndex(10),
      HnId(2) -> HnIndex(20),
      HnId(3) -> HnIndex(30)
    )

    def mkSampleEdge(e: SampleEdge.New): SampleEdge = SampleEdge(
      sampleId = sampleId,
      source = End(e.source, hnIndexMap(e.source)),
      target = End(e.target, hnIndexMap(e.target)),
      edgeType = e.edgeType
    )

    lazy val sample = Sample(
      data = SampleData(
        id = sampleId,
        probabilityCount = newSample.probabilityCount,
        utility = newSample.utility,
        name = newSample.name,
        description = newSample.description
      ),
      edges = newSample.edges.map(e => mkSampleEdge(e)).toSet
    )

    lazy val sampleDataMap = Map(sampleId -> sample.data)
    lazy val edgesMap = Map(sampleId -> sample.edges.toList)

  "Sample.allHnIds" should:
    "return all HN IDs" in newCase[CaseData]: (_, data) =>
      data.sample.allHnIds.pure[IO].asserting(_ mustEqual data.hnIndexMap.keySet)

  "Sample.validationName" should:
    "return validation name" in newCase[CaseData]: (_, data) =>
      data.sample.validationName.pure[IO].asserting(_ mustEqual "Sample(id=SampleId(100), name=SampleName)")

  "Sample.validationErrors" should:
    "return no error for valid sample" in newCase[CaseData]: (_, data) =>
      data.sample.validationErrors.pure[IO].asserting(_ mustBe empty)

    "return error if conflicting HnIndex values" in newCase[CaseData]: (_, data) =>
      val invalidEdge = SampleEdge(
        sampleId = data.sampleId,
        source = End(HnId(1), HnIndex(10)), // Conflicting index for HnId(1)
        target = End(HnId(1), HnIndex(20)),
        edgeType = EdgeType.LINK
      )

      data.sample.copy(edges = data.sample.edges + invalidEdge).validationErrors.pure[IO].asserting: errs =>
        errs.size mustBe 1
        errs.head.getMessage must include("Conflicting HnIndex values for")

    "return error if not all edges have the same SampleId" in newCase[CaseData]: (_, data) =>
      val invalidEdge = data.sample.edges.head.copy(sampleId = SampleId(-1))

      data.sample.copy(edges = data.sample.edges + invalidEdge).validationErrors.pure[IO].asserting: errs =>
        errs.size mustBe 1
        errs.head.getMessage must include("All SampleEdges must have the same SampleId")

  "Sample.formNew" should:
    "create sample from New" in newCase[CaseData]: (tn, data) =>
      Sample.formNew[IO](data.sampleId, data.newSample, data.hnIndexMap).logValue(tn, "validationName")
        .asserting(_ mustEqual data.sample)

  "Sample.formDataMap" should:
    "create Sample from data map" in newCase[CaseData]: (tn, data) =>
      Sample.formDataMap[IO](data.sampleId, data.sampleDataMap, data.edgesMap).logValue(tn, "formDataMap")
        .asserting(_ mustEqual data.sample)

    "fail if sample data missing in map" in newCase[CaseData]: (tn, data) =>
      Sample.formDataMap[IO](data.sampleId, data.sampleDataMap, Map()).logValue(tn, "formDataMap")
        .assertThrows[AssertionError]

    "fail if edges missing in map" in newCase[CaseData]: (tn, data) =>
      Sample.formDataMap[IO](data.sampleId, Map(), data.edgesMap).logValue(tn, "formDataMap")
        .assertThrows[AssertionError]

    "fail if edges have different sampleId" in newCase[CaseData]: (tn, data) =>
      val edge = data.sample.edges.head
      val invalidEdgesMap = Map(data.sampleId -> List(edge, edge))

      Sample.formDataMap[IO](data.sampleId, data.sampleDataMap, invalidEdgesMap).logValue(tn, "formDataMap")
        .assertThrows[AssertionError]

  "New.hnIds" should:
    "distinct list ofHnId" in newCase[CaseData]: (_, data) =>
      data.newSample.hnIds.pure[IO].asserting(_ mustEqual List(HnId(1), HnId(2), HnId(3)))

  "New.validationName" should:
    "return validation name" in newCase[CaseData]: (tn, data) =>
      data.newSample.validationName.pure[IO].logValue(tn, "validationName")
        .asserting(_ mustEqual "Sample.New(name=SampleName, probabilityCount=10, utility=0.5)")

  "New.validationErrors" should:
    "return empty list for valid data" in newCase[CaseData]: (_, data) =>
      data.newSample.validationErrors.pure[IO].asserting(_ mustEqual List())

    "return list errors for invalid data" in newCase[CaseData]: (tn, data) =>
      val invalidSample = data.newSample.copy(
        probabilityCount = 0, // Invalid count
        name = Name.some(""), // Empty name
        description = Description.some(""), // Empty description
        edges = List() // No edges
      )

      invalidSample.validationErrors.pure[IO].logValue(tn, "validationErrors").asserting(_.size mustEqual 4)

  "New.findHnIndexies" should:
    "return map of HnIndex" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val indicesMap = Map(
          HnId(1) -> List(HnIndex(10), HnIndex(11)),
          HnId(2) -> List(HnIndex(20), HnIndex(21)),
          HnId(3) -> List(HnIndex(30)),
          HnId(4) -> List(HnIndex(40)) // Not used in this sample
        )

        val (newIndicesMap, indices): (Map[HnId, List[HnIndex]], Map[HnId, HnIndex]) = data
          .newSample.findHnIndexies[IO](indicesMap).logValue(tn, "validationName").await

        newIndicesMap mustEqual Map(
          HnId(1) -> List(HnIndex(11)),
          HnId(2) -> List(HnIndex(21)),
          HnId(3) -> List(),
          HnId(4) -> List(HnIndex(40))
        )

        indices mustEqual Map(
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

  "New.toSampleData" should:
    "return SampleData with correct values" in newCase[CaseData]: (tn, data) =>
      data.newSample.toSampleData(data.sampleId).pure[IO].logValue(tn, "toSampleData")
        .asserting(_ mustEqual SampleData(
          id = data.sampleId,
          probabilityCount = data.newSample.probabilityCount,
          utility = data.newSample.utility,
          name = data.newSample.name,
          description = data.newSample.description
        ))

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
    "return correct aggregate values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val newSample2 = data.newSample.copy(edges = List(SampleEdge.New(HnId(3), HnId(4), EdgeType.LINK)))
        val newSamples = Sample.ListNew.of(data.newSample, newSample2)

        newSamples.allEdges mustEqual List(
          SampleEdge.New(HnId(1), HnId(2), EdgeType.LINK),
          SampleEdge.New(HnId(2), HnId(3), EdgeType.LINK),
          SampleEdge.New(HnId(3), HnId(4), EdgeType.LINK)
        )

        newSamples.allHnIds mustEqual List(HnId(1), HnId(2), HnId(3), HnId(4))
        newSamples.numHnIndexPerHn mustEqual Map(HnId(1) -> 1, HnId(2) -> 1, HnId(3) -> 2, HnId(4) -> 1)

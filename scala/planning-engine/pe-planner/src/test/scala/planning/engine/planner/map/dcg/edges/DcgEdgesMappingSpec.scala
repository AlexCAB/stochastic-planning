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
| created: 2026-01-16 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.values.node.HnId
import planning.engine.common.UnitSpecWithData
import planning.engine.common.validation.ValidationCheck
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds

class DcgEdgesMappingSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case:
    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)
    lazy val hnId5 = HnId(5)

    lazy val mapping: DcgEdgesMapping[IO] = DcgEdgesMapping(
      forward = Map(
        hnId1 -> Set(hnId2, hnId3)
      ),
      backward = Map(
        hnId2 -> Set(hnId1),
        hnId3 -> Set(hnId1)
      )
    )

    lazy val ends = Set(
      EndIds(hnId1, hnId4),
      EndIds(hnId1, hnId5),
      EndIds(hnId2, hnId4)
    )

  "DcgEdgesMapping.isEmpty" should:
    "be true for empty mapping" in newCase[CaseData]: (tn, data) =>
      DcgEdgesMapping.empty[IO].isEmpty.pure[IO].asserting(_ mustBe true)

    "be false for non-empty mapping" in newCase[CaseData]: (tn, data) =>
      data.mapping.isEmpty.pure[IO].asserting(_ mustBe false)

  "DcgEdgesMapping.allEnds" should:
    "return all ends in the mapping" in newCase[CaseData]: (tn, data) =>
      data.mapping.allEnds.pure[IO].asserting(_ mustBe Set(
        EndIds(data.hnId1, data.hnId2),
        EndIds(data.hnId1, data.hnId3)
      ))

  "DcgEdgesMapping.allHnIds" should:
    "return all HnIds in the mapping" in newCase[CaseData]: (tn, data) =>
      data.mapping.allHnIds.pure[IO].asserting(_ mustBe Set(data.hnId1, data.hnId2, data.hnId3))

  "DcgEdgesMapping.validationName" should:
    "be 'DcgEdgesMapping'" in newCase[CaseData]: (tn, data) =>
      data.mapping.checkValidationName("DcgEdgesMapping", tn)

  "DcgEdgesMapping.validationErrors" should:
    "be empty for valid mapping" in newCase[CaseData]: (tn, data) =>
      data.mapping.checkNoValidationError(tn)
    "be non-empty for invalid forward mapping" in newCase[CaseData]: (tn, data) =>
      val invalidMapping = DcgEdgesMapping[IO](
        forward = Map(data.hnId1 -> Set(data.hnId2)),
        backward = Map(data.hnId2 -> Set(data.hnId3))
      )
      invalidMapping.checkOneValidationError("Forward and Backward mappings keys/values mismatch", tn)

    "be non-empty for invalid backward mapping" in newCase[CaseData]: (tn, data) =>
      val invalidMapping = DcgEdgesMapping[IO](
        forward = Map(data.hnId1 -> Set(data.hnId2)),
        backward = Map(data.hnId3 -> Set(data.hnId1))
      )
      invalidMapping.checkOneValidationError("Backward and Forward mappings keys/values mismatch", tn)

  "DcgEdgesMapping.validateJoin(...)" should:
    "validate joining ids without conflicts" in newCase[CaseData]: (tn, data) =>
      data.mapping.validateJoin(
        acc = data.mapping.forward,
        hnId = data.hnId1,
        targets = Set(data.hnId5)
      ).pure[IO].asserting(_ mustBe true)

    "invalidate joining ids with conflicts" in newCase[CaseData]: (tn, data) =>
      data.mapping.validateJoin(
        acc = data.mapping.forward,
        hnId = data.hnId1,
        targets = Set(data.hnId3, data.hnId4)
      ).pure[IO].asserting(_ mustBe false)

  "DcgEdgesMapping.joinIds(...)" should:
    "join IDs references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(data.hnId1 -> Set(data.hnId2), data.hnId2 -> Set(data.hnId3))
      val newIds = Map(data.hnId1 -> Set(data.hnId4), data.hnId3 -> Set(data.hnId5))

      async[IO]:
        val joined = DcgEdgesMapping.empty[IO].joinIds(oldIds, newIds).await
        logInfo(tn, s"joined: $joined").await

        joined mustBe Map(
          data.hnId1 -> Set(data.hnId2, data.hnId4),
          data.hnId2 -> Set(data.hnId3),
          data.hnId3 -> Set(data.hnId5)
        )

    "fail if duplicate references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(data.hnId1 -> Set(data.hnId2))
      val newIds = Map(data.hnId1 -> Set(data.hnId2))

      DcgEdgesMapping.empty[IO].joinIds(oldIds, newIds).logValue(tn).assertThrows[AssertionError]

  "DcgEdgesMapping.addAll(...)" should:
    "add all edges to empty mapping" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val updated = DcgEdgesMapping.empty[IO].addAll(data.ends).await
        logInfo(tn, s"updated: $updated").await
        updated.checkNoValidationError(tn).await

        updated.forward mustBe Map(
          data.hnId1 -> Set(data.hnId4, data.hnId5),
          data.hnId2 -> Set(data.hnId4)
        )

        updated.backward mustBe Map(
          data.hnId4 -> Set(data.hnId1, data.hnId2),
          data.hnId5 -> Set(data.hnId1)
        )

    "add all edges to not empty mapping" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val updated = data.mapping.addAll(data.ends).await
        logInfo(tn, s"updated: $updated").await
        updated.checkNoValidationError(tn).await

        updated.forward mustBe Map(
          data.hnId1 -> Set(data.hnId2, data.hnId3, data.hnId4, data.hnId5),
          data.hnId2 -> Set(data.hnId4)
        )

        updated.backward mustBe Map(
          data.hnId2 -> Set(data.hnId1),
          data.hnId3 -> Set(data.hnId1),
          data.hnId4 -> Set(data.hnId1, data.hnId2),
          data.hnId5 -> Set(data.hnId1)
        )

  "DcgEdgesMapping.empty" should:
    "be an empty DcgEdgesMapping instance" in newCase[CaseData]: (tn, data) =>
      DcgEdgesMapping.empty[IO].pure[IO].logValue(tn).asserting: empty =>
        empty.forward mustBe Map.empty
        empty.backward mustBe Map.empty

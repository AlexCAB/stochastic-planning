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
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds

class DcgEdgesMappingSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val mapping: DcgEdgesMapping = DcgEdgesMapping(
      forward = Map(
        HnId(1) -> Set(HnId(2), HnId(3))
      ),
      backward = Map(
        HnId(2) -> Set(HnId(1)),
        HnId(3) -> Set(HnId(1))
      )
    )

    lazy val ends = Set(
      EndIds(HnId(1), HnId(4)),
      EndIds(HnId(1), HnId(5)),
      EndIds(HnId(2), HnId(4))
    )

  "DcgEdgesMapping.isEmpty(...)" should:
    "be true for empty mapping" in newCase[CaseData]: (tn, data) =>
      DcgEdgesMapping.empty.isEmpty.pure[IO].asserting(_ mustBe true)

    "be false for non-empty mapping" in newCase[CaseData]: (tn, data) =>
      data.mapping.isEmpty.pure[IO].asserting(_ mustBe false)

  "DcgEdgesMapping.validateJoin" should:
    "validate joining ids without conflicts" in newCase[CaseData]: (tn, data) =>
      data.mapping.validateJoin(
        acc = data.mapping.forward,
        hnId = HnId(1),
        targets = Set(HnId(5))
      ).pure[IO].asserting(_ mustBe true)

    "invalidate joining ids with conflicts" in newCase[CaseData]: (tn, data) =>
      data.mapping.validateJoin(
        acc = data.mapping.forward,
        hnId = HnId(1),
        targets = Set(HnId(3), HnId(4))
      ).pure[IO].asserting(_ mustBe false)

  "DcgEdgesMapping.joinIds(...)" should:
    "join IDs references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(HnId(1) -> Set(HnId(2)), HnId(2) -> Set(HnId(3)))
      val newIds = Map(HnId(1) -> Set(HnId(4)), HnId(3) -> Set(HnId(5)))

      async[IO]:
        val joined = DcgEdgesMapping.empty.joinIds[IO](oldIds, newIds).await
        logInfo(tn, s"joined: $joined").await

        joined mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(4)),
          HnId(2) -> Set(HnId(3)),
          HnId(3) -> Set(HnId(5))
        )

    "fail if duplicate references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(HnId(1) -> Set(HnId(2)))
      val newIds = Map(HnId(1) -> Set(HnId(2)))

      DcgEdgesMapping.empty.joinIds[IO](oldIds, newIds).logValue(tn).assertThrows[AssertionError]

  "DcgEdgesMapping.addAll(...)" should:
    "add all edges to empty mapping" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val updated = DcgEdgesMapping.empty.addAll[IO](data.ends).await
        logInfo(tn, s"updated: $updated").await

        updated.forward mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(3)),
          HnId(2) -> Set(HnId(4))
        )

        updated.backward mustBe Map(
          HnId(2) -> Set(HnId(1)),
          HnId(3) -> Set(HnId(1)),
          HnId(4) -> Set(HnId(2))
        )

    "add all edges to not empty mapping" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val updated = data.mapping.addAll[IO](data.ends).await
        logInfo(tn, s"updated: $updated").await

        updated.forward mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(3), HnId(4), HnId(5)),
          HnId(2) -> Set(HnId(4))
        )

        updated.backward mustBe Map(
          HnId(2) -> Set(HnId(1)),
          HnId(3) -> Set(HnId(1)),
          HnId(4) -> Set(HnId(1), HnId(2)),
          HnId(5) -> Set(HnId(1))
        )

  "DcgEdgesMapping.empty" should:
    "be an empty DcgEdgesMapping instance" in newCase[CaseData]: (tn, data) =>
      DcgEdgesMapping.empty.pure[IO].logValue(tn).asserting: empty =>
        empty.forward mustBe empty
        empty.backward mustBe empty

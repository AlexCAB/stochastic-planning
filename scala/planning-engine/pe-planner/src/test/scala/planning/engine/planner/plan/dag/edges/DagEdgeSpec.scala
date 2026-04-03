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
| created: 2026-04-04 |||||||||||*/

package planning.engine.planner.plan.dag.edges

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.PeKey.{Link, Then}
import planning.engine.common.values.node.MnId
import planning.engine.planner.plan.test.data.DaGraphTestData

class DagEdgeSpec extends UnitSpecWithData:

  private class CaseData extends Case with DaGraphTestData:
    lazy val linkKey = Link(pnId1, pnId3)
    lazy val thenKey = Then(pnId3, pnId1)

    lazy val linkEdge = new DagEdge[IO](linkKey, makeDcgEdgeThen(pnId1.mnId, pnId3.mnId, Iterable.empty))
    lazy val thenEdge = new DagEdge[IO](thenKey, makeDcgEdgeThen(pnId3.mnId, pnId1.mnId, Iterable.empty))

  "DagEdge.isLink" should:
    "return true for link edge and false for then" in newCase[CaseData]: (tn, data) =>
      data.linkEdge.isLink.pure[IO].logValue(tn).asserting(_ mustBe true)
      data.thenEdge.isLink.pure[IO].logValue(tn).asserting(_ mustBe false)

  "DagEdge.isThen" should:
    "return true for then edge and false for link" in newCase[CaseData]: (tn, data) =>
      data.linkEdge.isThen.pure[IO].logValue(tn).asserting(_ mustBe false)
      data.thenEdge.isThen.pure[IO].logValue(tn).asserting(_ mustBe true)

  "DagEdge.apply(...)" should:
    "create DagEdge if PeKey and DcgEdge MnIds match" in newCase[CaseData]: (tn, data) =>
      import data.*
      DagEdge[IO](linkKey, makeDcgEdgeThen(pnId1.mnId, pnId3.mnId, Iterable.empty)).logValue(tn)
        .asserting(_.key mustBe data.linkKey)

    "fail if PeKey and DcgEdge source MnIds do not match" in newCase[CaseData]: (tn, data) =>
      import data.*
      DagEdge[IO](linkKey, makeDcgEdgeThen(MnId.Con(999L), pnId3.mnId, Iterable.empty)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Source MnIds must match between PeKey and DcgEdge"))

    "fail if PeKey and DcgEdge target MnIds do not match" in newCase[CaseData]: (tn, data) =>
      import data.*
      DagEdge[IO](linkKey, makeDcgEdgeThen(pnId1.mnId, MnId.Con(999L), Iterable.empty)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Target MnIds must match between PeKey and DcgEdge"))

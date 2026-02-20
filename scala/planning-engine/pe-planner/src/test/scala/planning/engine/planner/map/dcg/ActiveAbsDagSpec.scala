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
| created: 2026-01-18 |||||||||||*/

package planning.engine.planner.map.dcg

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.edge.EdgeKeySet
import planning.engine.common.values.edge.EdgeKey.Then
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.test.data.DcgGraphTestData

class ActiveAbsDagSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case with DcgGraphTestData:
    lazy val mn11 = makeConDcgNode(id = MnId.Con(11))
    lazy val mn12 = makeConDcgNode(id = MnId.Con(12))

    lazy val dcgGraph = graphWithEdges.addNodes(List(mn11, mn12)).unsafeRunSync()
    lazy val backwordKeys = EdgeKeySet[Then](Then(mn11.id, mnId1), Then(mn12.id, mnId5))
    lazy val activeAbsDag = new ActiveAbsDag[IO](backwordKeys, dcgGraph)

  "ActiveAbsDag.apply(EdgeKeySet, DcgGraph)" should:
    "create instance from valid data" in newCase[CaseData]: (tn, data) =>
      import data.{backwordKeys, dcgGraph, activeAbsDag}
      ActiveAbsDag[IO](backwordKeys, dcgGraph).logValue(tn).asserting(_ mustBe activeAbsDag)

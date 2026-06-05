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

package planning.engine.planner.gsi.plan.dag.nodes

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.PnId
import planning.engine.planner.gsi.map.test.data.DcgNodeTestData

class DagNodeSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgNodeTestData:
    lazy val conPnId = PnId.Con(mnId1, 0L)
    lazy val absPnId = PnId.Abs(mnId3, 0L)

    lazy val testTime = Some(IoTime(123L))

    lazy val conDcgNode = makeConDcgNode(id = conPnId.mnId.value)
    lazy val absDcgNode = makeAbsDcgNode(id = absPnId.mnId.value)

    lazy val conNode = new DagNode[IO](conPnId, time = None, conDcgNode)
    lazy val absNode = new DagNode[IO](absPnId, testTime, absDcgNode)

  "DagNode.repr" should:
    "return true for link edge and false for then" in newCase[CaseData]: (tn, data) =>
      data.conNode.repr.pure[IO].logValue(tn)
        .asserting(_ mustBe "[t=?, i=0, 1, \"Con DCG Node 1\", <102 of boolInputNode>]")

      data.absNode.repr.pure[IO].logValue(tn)
        .asserting(_ mustBe "(t=123, i=0, 3, \"Abs DCG Node 3\")")

  "DagNode.apply" should:
    "create DagNode with correct fields" in newCase[CaseData]: (tn, data) =>
      import data.*
      DagNode[IO](absPnId, testTime, absDcgNode).logValue(tn).asserting(_ mustBe absNode)

    "fail if mnId not match in PnId and DcgNode" in newCase[CaseData]: (tn, data) =>
      import data.*
      DagNode[IO](conPnId, testTime, absDcgNode).logValue(tn)
        .assertThrowsError(_.getMessage must include("MnIds must match between PnId and DcgNode"))

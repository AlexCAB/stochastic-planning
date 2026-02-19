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
| created: 2026-02-02 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.test.data.DcgNodeTestData

class DcgNodeReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgNodeTestData:
    lazy val concreteNode: DcgNode.Concrete[IO] = makeConDcgNode()
      .copy[IO](id = MnId.Con(123), name = Some(HnName("TestConNode")))

    lazy val abstractNode: DcgNode.Abstract[IO] = makeAbsDcgNode()
      .copy[IO](id = MnId.Abs(123), name  = Some(HnName("TestAbsNode")))

  "DcgNodeRepr.repr" should:
    "return correct string representation for concrete node" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val withName = data.concreteNode.repr
        withName mustBe "[123, \"TestConNode\"]"

        val noName = data.concreteNode.copy[IO](id = MnId.Con(123), name = None).repr
        noName mustBe "[123]"

    "return correct string representation for abstract node" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val withName = data.abstractNode.repr
        withName mustBe "(123, \"TestAbsNode\")"

        val noName = data.abstractNode.copy[IO](id = MnId.Abs(123), name = None).repr
        noName mustBe "(123)"

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
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.test.data.MapDcgTestData

class DcgNodeReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapDcgTestData:
    lazy val concreteNode: DcgNode.Concrete[IO] = makeConcreteDcgNode()
      .copy[IO](id = HnId(123), name = Some(HnName("TestConNode")))

    lazy val abstractNode: DcgNode.Abstract[IO] = makeAbstractDcgNode()
      .copy[IO](id = HnId(123), name  = Some(HnName("TestAbsNode")))

  "DcgNodeRepr.repr" should:
    "return correct string representation for concrete node" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val withName = data.concreteNode.repr
        withName mustBe "[123, \"TestConNode\"]"

        val noName = data.concreteNode.copy[IO](id = HnId(123), name = None).repr
        noName mustBe "[123]"

    "return correct string representation for abstract node" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val withName = data.abstractNode.repr
        withName mustBe "(123, \"TestAbsNode\")"

        val noName = data.abstractNode.copy[IO](id = HnId(123), name = None).repr
        noName mustBe "(123)"

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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map.dcg.nodes

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoName, IoValue}
import planning.engine.common.values.node.{MnId, HnName}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.test.data.MapNodeTestData

class DcgNodeSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapNodeTestData:
    lazy val concreteNode: ConcreteNode[IO] = makeConcreteNode()
    lazy val conNodeNew = ConcreteNode.New(
      name = concreteNode.name,
      description = concreteNode.description,
      ioNodeName = concreteNode.ioNode.name,
      valueIndex = concreteNode.valueIndex
    )

    def getIoNode(name: IoName): IO[IoNode[IO]] =
      if name == concreteNode.ioNode.name then concreteNode.ioNode.pure
      else fail(s"Unexpected IoName: $name")

    lazy val abstractNode: AbstractNode[IO] = makeAbstractNode()
    lazy val absNodeNew = AbstractNode.New(
      name = abstractNode.name,
      description = abstractNode.description
    )

    lazy val conDcgNode = DcgNode.Concrete[IO](concreteNode.id.asCon, conNodeNew, getIoNode).unsafeRunSync()
    lazy val absDcgNode = DcgNode.Abstract[IO](abstractNode.id.asAbs, absNodeNew).unsafeRunSync()

  "DcgNode.Concrete.apply(ConcreteNode)" should:
    "crete DcgNode.Concrete correctly from ConcreteNode" in newCase[CaseData]: (tn, data) =>
      import data.concreteNode
      async[IO]:
        val conNode = DcgNode.Concrete[IO](concreteNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe concreteNode.id.asCon
        conNode.name mustBe concreteNode.name
        conNode.ioNode mustBe concreteNode.ioNode
        conNode.ioValue mustBe concreteNode.ioValue

  "DcgNode.Concrete.apply(MnId, ConcreteNode.New, getIoNode)" should:
    "create DcgNode.Concrete correctly from ConcreteNode.New" in newCase[CaseData]: (tn, data) =>
      import data.{concreteNode, conNodeNew, getIoNode}
      async[IO]:
        val conNode = DcgNode.Concrete[IO](concreteNode.id.asCon, conNodeNew, getIoNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe concreteNode.id.asCon
        conNode.name mustBe concreteNode.name
        conNode.ioNode mustBe concreteNode.ioNode
        conNode.ioValue mustBe concreteNode.ioValue

  "DcgNode.Concrete.ioValue" should:
    "lazily load ioValue when accessed" in newCase[CaseData]: (tn, data) =>
      import data.{conDcgNode, concreteNode}
      conDcgNode.ioValue.pure[IO].logValue(tn)
        .asserting(_ mustBe IoValue(concreteNode.ioNode.name, concreteNode.valueIndex))

  "DcgNode.Abstract.apply(AbstractNode)" should:
    "crete DcgNode.Abstract correctly from AbstractNode" in newCase[CaseData]: (tn, data) =>
      import data.abstractNode
      async[IO]:
        val absNode = DcgNode.Abstract[IO](abstractNode).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe abstractNode.id.asAbs
        absNode.name mustBe abstractNode.name

  "DcgNode.Abstract.apply(MnId, AbstractNode.New)" should:
    "crete DcgNode.Abstract correctly from AbstractNode.New" in newCase[CaseData]: (tn, data) =>
      import data.{abstractNode, absNodeNew}
      async[IO]:
        val absNode = DcgNode.Abstract[IO](abstractNode.id.asAbs, absNodeNew).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe abstractNode.id.asAbs
        absNode.name mustBe abstractNode.name

  "DcgNode.Abstract.repr" should:
    "return correct string representation" in newCase[CaseData]: (_, data) =>
      import data.absDcgNode
      async[IO]:
        val withName = absDcgNode.copy[IO](id = MnId.Abs(456), name = Some(HnName("TestAbsNode"))).repr
        withName mustBe "(456, \"TestAbsNode\")"

        val noName = absDcgNode.copy[IO](id = MnId.Abs(456), name = None).repr
        noName mustBe "(456)"

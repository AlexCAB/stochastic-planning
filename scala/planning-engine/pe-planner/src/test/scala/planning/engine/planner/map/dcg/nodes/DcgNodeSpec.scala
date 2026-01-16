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
import planning.engine.common.values.io.IoName
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.test.data.MapNodeTestData

class DcgNodeSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapNodeTestData:
    lazy val concreteNode: ConcreteNode[IO] = makeConcreteNode()
    lazy val nodeNew = ConcreteNode.New(
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

  "DcgNode.Concrete.apply(ConcreteNode)" should:
    "crete DcgNode.Concrete correctly from ConcreteNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = DcgNode.Concrete[IO](data.concreteNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe data.concreteNode.id
        conNode.name mustBe data.concreteNode.name
        conNode.ioNode mustBe data.concreteNode.ioNode
        conNode.ioValue mustBe data.concreteNode.ioValue

  "DcgNode.Concrete.apply(hnId, ConcreteNode.New, getIoNode)" should:
    "create DcgNode.Concrete correctly from ConcreteNode.New" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = DcgNode.Concrete[IO](data.concreteNode.id, data.nodeNew, data.getIoNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe data.concreteNode.id
        conNode.name mustBe data.concreteNode.name
        conNode.ioNode mustBe data.concreteNode.ioNode
        conNode.ioValue mustBe data.concreteNode.ioValue

  "DcgNode.Abstract.apply(AbstractNode)" should:
    "crete DcgNode.Abstract correctly from AbstractNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = DcgNode.Abstract[IO](data.abstractNode).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe data.abstractNode.id
        absNode.name mustBe data.abstractNode.name

  "DcgNode.Abstract.apply(HnId, AbstractNode.New)" should:
    "crete DcgNode.Abstract correctly from AbstractNode.New" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = DcgNode.Abstract[IO](data.abstractNode.id, data.absNodeNew).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe data.abstractNode.id
        absNode.name mustBe data.abstractNode.name

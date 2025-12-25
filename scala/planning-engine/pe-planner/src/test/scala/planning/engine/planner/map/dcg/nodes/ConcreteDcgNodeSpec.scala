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
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoName
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.test.data.MapNodeTestData

class ConcreteDcgNodeSpec extends UnitSpecWithData:

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

  "ConcreteDcgNode.apply(ConcreteNode)" should:
    "crete ConcreteDcgNode correctly from ConcreteNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = ConcreteDcgNode[IO](data.concreteNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe data.concreteNode.id
        conNode.name mustBe data.concreteNode.name
        conNode.ioNode mustBe data.concreteNode.ioNode
        conNode.ioValue mustBe data.concreteNode.ioValue

  "ConcreteDcgNode.apply(hnId, ConcreteNode.New, getIoNode)" should:
    "create ConcreteDcgNode correctly from ConcreteNode.New" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = ConcreteDcgNode[IO](data.concreteNode.id, data.nodeNew, data.getIoNode).await
        logInfo(tn, s"conNode: $conNode").await

        conNode.id mustBe data.concreteNode.id
        conNode.name mustBe data.concreteNode.name
        conNode.ioNode mustBe data.concreteNode.ioNode
        conNode.ioValue mustBe data.concreteNode.ioValue

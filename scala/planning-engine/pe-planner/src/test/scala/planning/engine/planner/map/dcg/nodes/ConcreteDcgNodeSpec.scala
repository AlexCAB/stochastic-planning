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
import planning.engine.common.UnitSpecWithData
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.planner.map.MapTestData

class ConcreteDcgNodeSpec extends UnitSpecWithData with MapTestData:

  private class CaseData extends Case:
    lazy val concreteNode: ConcreteNode[IO] = makeConcreteNode()

  "ConcreteDcgNode.apply(...)" should:
    "crete ConcreteDcgNode correctly from ConcreteNode" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val conNode = ConcreteDcgNode[IO](data.concreteNode).await
        logInfo(n, s"conNode: $conNode").await

        conNode.id mustBe data.concreteNode.id
        conNode.name mustBe data.concreteNode.name
        conNode.ioNode mustBe data.concreteNode.ioNode
        conNode.ioValue mustBe data.concreteNode.ioValue

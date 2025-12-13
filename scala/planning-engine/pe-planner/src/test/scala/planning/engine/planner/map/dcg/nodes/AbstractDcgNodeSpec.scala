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
import planning.engine.map.hidden.node.AbstractNode
import planning.engine.planner.map.MapTestData

class AbstractDcgNodeSpec extends UnitSpecWithData with MapTestData:

  private class CaseData extends Case:
    lazy val abstractNode: AbstractNode[IO] = makeAbstractNode()

  "AbstractDcgNode.apply(...)" should:
    "crete AbstractDcgNode correctly from AbstractNode" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val absNode = AbstractDcgNode[IO](data.abstractNode).await
        logInfo(n, s"absNode: $absNode").await

        absNode.id mustBe data.abstractNode.id
        absNode.name mustBe data.abstractNode.name

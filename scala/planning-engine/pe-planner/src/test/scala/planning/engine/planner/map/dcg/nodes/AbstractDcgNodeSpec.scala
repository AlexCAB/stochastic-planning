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
import planning.engine.planner.map.test.data.MapNodeTestData

class AbstractDcgNodeSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapNodeTestData:
    lazy val abstractNode: AbstractNode[IO] = makeAbstractNode()
    lazy val absNodeNew = AbstractNode.New(
      name = abstractNode.name,
      description = abstractNode.description
    )

  "AbstractDcgNode.apply(AbstractNode)" should:
    "crete AbstractDcgNode correctly from AbstractNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = AbstractDcgNode[IO](data.abstractNode).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe data.abstractNode.id
        absNode.name mustBe data.abstractNode.name

  "AbstractDcgNode.apply(HnId, AbstractNode.New)" should:
    "crete AbstractDcgNode correctly from AbstractNode.New" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = AbstractDcgNode[IO](data.abstractNode.id, data.absNodeNew).await
        logInfo(tn, s"absNode: $absNode").await

        absNode.id mustBe data.abstractNode.id
        absNode.name mustBe data.abstractNode.name

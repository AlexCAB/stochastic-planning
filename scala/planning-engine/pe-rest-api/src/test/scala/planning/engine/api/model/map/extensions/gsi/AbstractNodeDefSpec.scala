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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.api.model.map.extensions.gsi.AbstractNodeDef.toNew
import planning.engine.api.model.map.payload.AbstractNodeDef
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.AbstractNode

class AbstractNodeDefSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testAbstractNodeDef = AbstractNodeDef(HnName("abstractNode"), Description.some("testAbstractNodeDef"))

  "AbstractNodeDef.toNew" should:
    "convert to AbstractNode.New" in newCase[CaseData]: (_, data) =>
      data.testAbstractNodeDef.toNew.pure[IO].asserting(_ mustEqual AbstractNode.New(
        name = Some(data.testAbstractNodeDef.name),
        description = data.testAbstractNodeDef.description,
      ))

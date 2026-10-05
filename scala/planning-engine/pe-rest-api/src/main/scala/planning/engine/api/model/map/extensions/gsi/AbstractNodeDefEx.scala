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

import planning.engine.api.model.map.payload.AbstractNodeDef
import planning.engine.map.hidden.node.AbstractNode

object AbstractNodeDefEx:
  extension (node: AbstractNodeDef)
    def toNew: AbstractNode.New = AbstractNode.New(name = Some(node.name), description = node.description)

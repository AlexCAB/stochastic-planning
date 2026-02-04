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

import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.nodes.DcgNode.{Abstract, Concrete}

trait DcgNodeRepr[F[_]]:
  self: DcgNode[F] =>

  lazy val repr: String = self match
    case _: Concrete[F] => s"[${id.value}${name.repr}]"
    case _: Abstract[F] => s"(${id.value}${name.repr})"

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

package planning.engine.planner.map.repr

import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.nodes.DcgNode.{Abstract, Concrete}

trait DcgNodeRepr[F[_]]:
  self: DcgNode[F] =>

  lazy val repr: String = self match
    case cn: Concrete[F] => s"[$idRepr, ${cn.ioValue.repr}]"
    case _: Abstract[F]  => s"($idRepr)"

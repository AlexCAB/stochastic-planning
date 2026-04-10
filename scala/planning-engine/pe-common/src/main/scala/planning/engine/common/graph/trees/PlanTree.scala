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
| created: 2026-04-08 |||||||||||*/

package planning.engine.common.graph.trees

import planning.engine.common.values.node.PnId

final case class PlanTree(root: PlanTree.Root)

object PlanTree:

  sealed trait Node:
    def pnId: PnId

  final case class Root(next: List[Node], pnId: PnId) extends Node
  final case class Vertex(prev: Node, next: List[Node], pnId: PnId) extends Node
  final case class Leaf(prev: Node, pnId: PnId) extends Node

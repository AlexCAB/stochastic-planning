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

import planning.engine.common.graph.trees.PlanTree.Vertex
import planning.engine.common.values.node.PnId

final case class PlanTree(root: Vertex):
  def buildRepr(renderNode: PnId => String, arrow: String): List[String] =
    def trace(current: Vertex, prevStr: String): List[String] = current.next match
      case oneNext :: Nil => trace(oneNext, s"$prevStr${renderNode(current.pnId)}$arrow")

      case firstNext :: midNext =>
        val initStr = prevStr + renderNode(current.pnId)
        val prefix = " " * initStr.length
        List(
          trace(firstNext, initStr + "+" + arrow),
          midNext.dropRight(1).flatMap(next => trace(next, arrow)).map(str => prefix + "|" + str),
          trace(midNext.last, prefix + "\\" + arrow),
        ).flatten

      case Nil => List(prevStr + renderNode(current.pnId))

    trace(root, "")

  override lazy val toString: String = "PlanTree:\n" + buildRepr(_.repr, "-->").map(l => "  " + l).mkString("\n")

object PlanTree:
  final case class Vertex(next: List[Vertex], pnId: PnId)

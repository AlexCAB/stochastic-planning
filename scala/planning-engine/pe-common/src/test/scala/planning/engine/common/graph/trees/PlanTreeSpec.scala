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
| created: 2026-04-12 |||||||||||*/

package planning.engine.common.graph.trees

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.node.{MnId, PnId}

class PlanTreeSpec extends UnitSpecIO:
  import PlanTree.*

  def makePnId(id: Long): PnId = PnId.Abs(MnId.Abs(id), 0L)
  def makeVertex(id: Long, child: Vertex*): Vertex = Vertex(child.toList, makePnId(id))
  def makeLeaf(id: Long): Vertex = Vertex(List(), makePnId(id))

  lazy val planTree = PlanTree(
    root = makeVertex(
      0,
      makeVertex(1, makeLeaf(11)),
      makeVertex(2, makeVertex(21, makeVertex(211, makeLeaf(2111))), makeVertex(22, makeLeaf(221), makeLeaf(222))),
      makeVertex(3, makeVertex(31, makeLeaf(311)), makeVertex(32, makeLeaf(321)))
    )
  )

  "PlanTree.buildRepr" should:
    "return correct string representation" in: tn =>
      ("\n" + planTree.buildRepr(_.repr, "-->").mkString("\n")).pure[IO].logValue(tn)
        .asserting(_ must include("(0,i=0)+-->(1,i=0)-->(11,i=0)"))

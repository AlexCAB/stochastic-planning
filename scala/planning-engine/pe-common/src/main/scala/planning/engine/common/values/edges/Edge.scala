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
| created: 2026-01-26 |||||||||||*/

package planning.engine.common.values.edges

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.HnId

sealed trait Edge:
  def src: HnId
  def trg: HnId

  def srcEnd: End
  def trgEnd: End

  lazy val ends: Edge.Ends = Edge.Ends(src, trg)

  lazy val eType: EdgeType = this match
    case _: Edge.Link => EdgeType.LINK
    case _: Edge.Then => EdgeType.THEN

  lazy val repr: String =
    val arrow = this match
      case _: Edge.Link => "-link->"
      case _: Edge.Then => "-then->"

    s"(${src.vStr}) $arrow (${trg.vStr})"

object Edge:
  final case class Link(src: HnId, trg: HnId) extends Edge:
    lazy val srcEnd: End.Link = End.Link(src)
    lazy val trgEnd: End.Link = End.Link(trg)

  final case class Then(src: HnId, trg: HnId) extends Edge:
    lazy val srcEnd: End.Then = End.Then(src)
    lazy val trgEnd: End.Then = End.Then(trg)

  final case class Ends(src: HnId, trg: HnId):
    lazy val swap: Ends = Ends(trg, src)

    lazy val toLink: Link = Link(src, trg)
    lazy val toThen: Then = Then(src, trg)

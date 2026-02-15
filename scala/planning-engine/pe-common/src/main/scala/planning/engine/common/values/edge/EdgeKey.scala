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

package planning.engine.common.values.edge

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.MnId

sealed trait EdgeKey:
  def src: MnId
  def trg: MnId

  def srcEnd: EdgeKey.End
  def trgEnd: EdgeKey.End

  lazy val asEdgeType: EdgeType = this match
    case _: EdgeKey.Link => EdgeType.LINK
    case _: EdgeKey.Then => EdgeType.THEN

  lazy val repr: String =
    val arrow = this match
      case _: EdgeKey.Link => "-link->"
      case _: EdgeKey.Then => "-then->"

    s"${src.reprNode} $arrow ${trg.reprNode}"

object EdgeKey:
  sealed trait End:
    def id: MnId
    def asSrcKey(src: MnId): EdgeKey
    def asTrgKey(trg: MnId): EdgeKey

  final case class Link(src: MnId, trg: MnId) extends EdgeKey:
    lazy val srcEnd: Link.End = Link.End(src)
    lazy val trgEnd: Link.End = Link.End(trg)

  object Link:
    final case class End(id: MnId) extends EdgeKey.End:
      def asSrcKey(src: MnId): Link = Link(src, id)
      def asTrgKey(trg: MnId): Link = Link(id, trg)

  final case class Then(src: MnId, trg: MnId) extends EdgeKey:
    lazy val srcEnd: Then.End = Then.End(src)
    lazy val trgEnd: Then.End = Then.End(trg)

  object Then:
    final case class End(id: MnId) extends EdgeKey.End:
      def asSrcKey(src: MnId): Then = Then(src, id)
      def asTrgKey(trg: MnId): Then = Then(id, trg)

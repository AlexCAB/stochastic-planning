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

package planning.engine.common.graph.edges

import cats.MonadThrow
import cats.syntax.all.*

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, MnId}
import planning.engine.common.values.node.MnId.{Con, Abs}

sealed trait EdgeKey:
  def src: MnId
  def trg: MnId

  def srcEnd: EdgeKey.End
  def trgEnd: EdgeKey.End

  def asKey: EdgeKey = this

  lazy val mnIds: Set[MnId] = Set(src, trg)

  lazy val isLink: Boolean = this.isInstanceOf[EdgeKey.Link]
  lazy val isThen: Boolean = this.isInstanceOf[EdgeKey.Then]

  lazy val asEdgeType: EdgeType = this match
    case _: EdgeKey.Link => EdgeType.LINK
    case _: EdgeKey.Then => EdgeType.THEN

  lazy val reprArrow: String = this match
    case _: EdgeKey.Link => "-link->"
    case _: EdgeKey.Then => "-then->"

  override def toString: String = s"${src.reprNode} $reprArrow ${trg.reprNode}"

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

  def apply[F[_]: MonadThrow](et: EdgeType, src: HnId, trg: HnId, conMnId: Set[Con], absMnId: Set[Abs]): F[EdgeKey] =
    for
      srcMnId <- src.toMnId(conMnId, absMnId)
      trgMnId <- trg.toMnId(conMnId, absMnId)
    yield et match
      case EdgeType.LINK => Link(srcMnId, trgMnId)
      case EdgeType.THEN => Then(srcMnId, trgMnId)

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
import planning.engine.common.errors.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, MnId}
import planning.engine.common.values.node.MnId.{Con, Abs}

sealed trait MeKey:
  import MnId.Nim
  
  def src: MnId
  def trg: MnId

  def srcEnd: MeKey.End
  def trgEnd: MeKey.End

  lazy val mnIds: Set[MnId] = Set(src, trg)

  lazy val isLink: Boolean = this.isInstanceOf[MeKey.Link]
  lazy val isThen: Boolean = this.isInstanceOf[MeKey.Then]

  lazy val asEdgeType: EdgeType = this match
    case _: MeKey.Link => EdgeType.LINK
    case _: MeKey.Then => EdgeType.THEN

  lazy val reprArrow: String = this match
    case _: MeKey.Link => "=link=>"
    case _: MeKey.Then => "-then->"

  lazy val repr: String = s"${src.reprNode}$reprArrow${trg.reprNode}"
  
  override def toString: String = repr
  def asKey: MeKey = this
  
  def resolve[F[_]: MonadThrow](idsMap: Map[Nim, MnId]): F[MeKey]
  
  protected def resolveId[F[_]: MonadThrow](id: MnId, idsMap: Map[Nim, MnId]): F[MnId] = id match
    case nim: Nim => idsMap.get(nim) match
      case Some(mnId) => mnId.pure
      case None       => s"MnId for $nim not found in $idsMap".assertionError
    case other => other.pure

  protected def resolveKey[F[_]: MonadThrow, K](idsMap: Map[Nim, MnId], make: (MnId, MnId) => K): F[K] =
    for
      resSrc <- resolveId(src, idsMap)
      resTrg <- resolveId(trg, idsMap)
    yield make(resSrc, resTrg)

object MeKey:
  import MnId.Nim
  
  sealed trait End:
    def id: MnId
    def asSrcKey(src: MnId): MeKey
    def asTrgKey(trg: MnId): MeKey

    lazy val repr: String = this match
      case _: Link.End => "==>" + id.reprNode
      case _: Then.End => "-->" + id.reprNode

  final case class Link(src: MnId, trg: MnId) extends MeKey:
    lazy val srcEnd: Link.End = Link.End(src)
    lazy val trgEnd: Link.End = Link.End(trg)

    override def resolve[F[_]: MonadThrow](idsMap: Map[Nim, MnId]): F[MeKey] = resolveKey(idsMap, Link.apply)

  object Link:
    final case class End(id: MnId) extends MeKey.End:
      def asSrcKey(src: MnId): Link = Link(src, id)
      def asTrgKey(trg: MnId): Link = Link(id, trg)

  final case class Then(src: MnId, trg: MnId) extends MeKey:
    lazy val srcEnd: Then.End = Then.End(src)
    lazy val trgEnd: Then.End = Then.End(trg)

    override def resolve[F[_]: MonadThrow](idsMap: Map[Nim, MnId]): F[MeKey] = resolveKey(idsMap, Then.apply)

  object Then:
    final case class End(id: MnId) extends MeKey.End:
      def asSrcKey(src: MnId): Then = Then(src, id)
      def asTrgKey(trg: MnId): Then = Then(id, trg)

  def apply[F[_]: MonadThrow](et: EdgeType, src: HnId, trg: HnId, conMnId: Set[Con], absMnId: Set[Abs]): F[MeKey] =
    for
      srcMnId <- src.toMnId(conMnId, absMnId)
      trgMnId <- trg.toMnId(conMnId, absMnId)
    yield et match
      case EdgeType.LINK => Link(srcMnId, trgMnId)
      case EdgeType.THEN => Then(srcMnId, trgMnId)

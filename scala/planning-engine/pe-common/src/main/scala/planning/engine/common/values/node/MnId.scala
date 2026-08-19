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
| created: 2026-02-05 |||||||||||*/

package planning.engine.common.values.node

// Same as HnId, but stronger typed for use in the planning engine.
// MnId.Con - Concrete node ID.
// MnId.Abs - Abstract node ID.
// MnId.Nim - Not in map node ID, used for new or virtual nodes.
sealed trait MnId extends Any:
  def value: Long

  def asHnId: HnId = HnId(value)
  def asMnId: MnId = this

  def isCon: Boolean = this.isInstanceOf[MnId.Con]
  def isAbs: Boolean = this.isInstanceOf[MnId.Abs]
  def isNim: Boolean = this.isInstanceOf[MnId.Nim]

  lazy val reprValue: String = value.toString

  lazy val reprNode: String = this match
    case _: MnId.Con => s"[$reprValue]"
    case _: MnId.Abs => s"($reprValue)"
    case _: MnId.Nim => s"{$reprValue}"

  override def toString: String = reprNode

object MnId:
  final case class Con(value: Long) extends AnyVal with MnId
  final case class Abs(value: Long) extends AnyVal with MnId
  final case class Nim(value: Long) extends AnyVal with MnId

  extension (mnIds: Set[MnId])
    def filterCon: Set[MnId.Con] = mnIds.collect { case con: MnId.Con => con }
    def filterAbs: Set[MnId.Abs] = mnIds.collect { case abs: MnId.Abs => abs }
    def filterNim: Set[MnId.Nim] = mnIds.collect { case nim: MnId.Nim => nim }
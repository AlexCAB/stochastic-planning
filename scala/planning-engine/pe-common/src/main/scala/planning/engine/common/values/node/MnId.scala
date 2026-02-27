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
sealed trait MnId:
  def value: Long

  lazy val asHnId: HnId = HnId(value)
  def asMnId: MnId = this

  lazy val isCon: Boolean = this.isInstanceOf[MnId.Con]
  lazy val isAbs: Boolean = this.isInstanceOf[MnId.Abs]

  lazy val reprValue: String = value.toString

  lazy val reprNode: String = this match
    case _: MnId.Con => s"[$reprValue]"
    case _: MnId.Abs => s"($reprValue)"

  override def toString: String = reprNode

object MnId:
  final case class Con(value: Long) extends MnId

  final case class Abs(value: Long) extends MnId

  extension (mnIds: Set[MnId])
    def filterCon: Set[MnId.Con] = mnIds.collect { case con: MnId.Con => con }
    def filterAbs: Set[MnId.Abs] = mnIds.collect { case abs: MnId.Abs => abs }

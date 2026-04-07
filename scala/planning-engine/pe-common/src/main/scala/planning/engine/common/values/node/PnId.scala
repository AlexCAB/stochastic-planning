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
| created: 2025-08-23 |||||||||||*/

package planning.engine.common.values.node

// Plan Node (DagNode) ID in the planning DAG.
sealed trait PnId:

  // ID of the map DcgNode (abstract or concrete) corresponding to the node in the planning DAG.
  def mnId: MnId

  // Count of usages of DcgNode in the planning DAG.
  // Using this count latest usage of DcgNode in the planning DAG can be identified.
  def count: Long

  def asPnId: PnId = this

  lazy val reprCount: String = s"i=$count"

  lazy val repr: String = mnId match
    case _: MnId.Con => s"[${mnId.reprValue},$reprCount]"
    case _: MnId.Abs => s"(${mnId.reprValue},$reprCount)"

  override def toString: String = repr

object PnId:
  final case class Con(mnId: MnId.Con, count: Long) extends PnId
  final case class Abs(mnId: MnId.Abs, count: Long) extends PnId

  extension (pnIds: Set[PnId])
    def filterCon: Set[PnId.Con] = pnIds.collect { case con: PnId.Con => con }
    def filterAbs: Set[PnId.Abs] = pnIds.collect { case abs: PnId.Abs => abs }

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

import planning.engine.common.values.io.IoTime

// Plan Node ID in the planning DAG.
// It consists of the MnId of the node and the step/world time (for each step here will be unique set of MnId).
sealed trait PnId:
  def mnId: MnId
  def time: IoTime

  def asPnId: PnId = this

  lazy val repr: String = s"${mnId.reprNode}_${time.repr}"
  override def toString: String = repr

object PnId:
  final case class Con(mnId: MnId.Con, time: IoTime) extends PnId
  final case class Abs(mnId: MnId.Abs, time: IoTime) extends PnId

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
| created: 2025-05-11 |||||||||||*/

package planning.engine.common.values.node

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.LongVal
import planning.engine.common.errors.assertionError

// Hidden Node ID is used to identify the hidden node in the graph.
final case class HnId(value: Long) extends AnyVal with LongVal:
  def increase: HnId = HnId(value + 1L)
  def asCon: MnId.Con = MnId.Con(value)
  def asAbs: MnId.Abs = MnId.Abs(value)

  def toMnId[F[_]: MonadThrow](conIds: Set[MnId.Con], absIds: Set[MnId.Abs]): F[MnId] =
    (conIds.contains(this.asCon), absIds.contains(this.asAbs)) match
      case (true, false)  => this.asCon.pure
      case (false, true)  => this.asAbs.pure
      case (true, true)   => s"Provided $this is both Con and Abs".assertionError
      case (false, false) => s"Provided $this is neither Con nor Abs".assertionError

object HnId:
  val init: HnId = HnId(1L)

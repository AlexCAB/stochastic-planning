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
| created: 2026-10-14 |||||||||||*/

package planning.engine.common.values.plan

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.common.errors.assertionError
import planning.engine.common.values.LongVal

// Weight implementation based on the Long type for Huang termination-detection algorithm.
final case class HuangWeight(value: Long) extends AnyVal with LongVal:
  def div[F[_]: MT](n: Int): F[List[HuangWeight]] =
    if n <= value then
      MT:
        val q = value / n
        val r = value % n
        List.fill(r.toInt)(new HuangWeight(q + 1)) ++ List.fill(n - r.toInt)(new HuangWeight(q))
    else s"Value is too small: $n > $value".assertionError

  def add[F[_]: MT](that: HuangWeight): F[HuangWeight] = MT(Math.addExact(value, that.value)).map(new HuangWeight(_))

  def isFull: Boolean = value == Long.MaxValue

object HuangWeight:
  val init: HuangWeight = new HuangWeight(Long.MaxValue)
  val zero: HuangWeight = new HuangWeight(0)

  def apply[F[_]: MT](value: Long): F[HuangWeight] =
    if value > 0 then new HuangWeight(value).pure
    else s"HuangWeight value must be positive, got: $value".assertionError

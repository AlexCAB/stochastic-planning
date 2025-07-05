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
| created: 2025-04-07 |||||||||||*/

package planning.engine.common.values.sample

import cats.ApplicativeThrow
import planning.engine.common.values.LongVal
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

final case class SampleId(value: Long) extends AnyVal with LongVal:
  def toPropName: String = "s" + value

object SampleId:
  val init: SampleId = SampleId(1L)

  def fromPropName[F[_]: ApplicativeThrow](str: String): F[SampleId] =
    if str.startsWith("s") && str.length > 1 && str.tail.forall(_.isDigit)
    then SampleId(str.tail.toLong).pure
    else s"Invalid SampleId property name: $str".assertionError

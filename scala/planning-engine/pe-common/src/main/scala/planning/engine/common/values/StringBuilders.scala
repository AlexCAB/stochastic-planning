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
| created: 2025-05-17 |||||||||||*/

package planning.engine.common.values

import cats.ApplicativeThrow
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

trait StringBuilders[T <: AnyVal]:
  protected def makeValue(str: String): T

  def fromString[F[_]: ApplicativeThrow](value: Option[String]): F[Option[T]] =
    value.map(str => fromString(str).map(_.some)).getOrElse(None.pure)

  def fromString[F[_]: ApplicativeThrow](str: String): F[T] =
    if str.nonEmpty then makeValue(str).pure else s"Can't build ${this.getClass} from empty string".assertionError

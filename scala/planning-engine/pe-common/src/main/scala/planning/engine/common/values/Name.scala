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
| created: 2025-04-27 |||||||||||*/

package planning.engine.common.values

import cats.ApplicativeThrow
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

final case class Name(value: String) extends AnyVal

object Name:
  def fromString[F[_]: ApplicativeThrow](value: String): F[Name] =
    if value.nonEmpty then Name(value).pure[F]
    else "Name cannot be empty".assertionError

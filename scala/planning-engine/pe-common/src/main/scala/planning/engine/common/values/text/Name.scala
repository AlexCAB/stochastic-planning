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
| created: 2025-04-08 |||||||||||*/

package planning.engine.common.values.text

import cats.ApplicativeThrow
import planning.engine.common.values.StringVal
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

final case class Name(value: String) extends AnyVal with StringVal

object Name:
  def fromStringOptional(value: String): Option[Name] = if value.nonEmpty then Some(Name(value)) else None
  def fromOptionString(value: Option[String]): Option[Name] =
    value.flatMap(v => if v.nonEmpty then Some(Name(v)) else None)

  def fromStringValid[F[_]: ApplicativeThrow](value: String): F[Name] =
    if value.nonEmpty then Name(value).pure else "Empty name".assertionError

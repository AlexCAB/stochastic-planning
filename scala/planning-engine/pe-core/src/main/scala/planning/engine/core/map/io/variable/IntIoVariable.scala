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
| created: 2025-03-20 |||||||||||*/


package planning.engine.core.map.io.variable

import cats.{ApplicativeThrow, MonadThrow}
import neotypes.model.types.Value
import planning.engine.common.values.Index
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import cats.syntax.all.*


class IntIoVariable[F[_] : ApplicativeThrow](val min: Int, val max: Int) extends IoVariable[F, Int]:
  override def valueForIndex(index: Index): F[Int] = index.value match
    case v if v >= min && v <= max => v.toInt.pure
    case v => s"Value $v of index $index not in range: $min to $max".assertionError

  override def indexForValue(value: Int): F[Index] = value match
    case v if v >= min && v <= max => Index(v).pure
    case v => s"Value $v not in range: $min to $max".assertionError

  override def toProperties: F[Map[String, Value]] = propsOf(
    "type" -> Value.Str("int"),
    "min" -> Value.Integer(min),
    "max" -> Value.Integer(max))

  override def toString: String = s"IntIoVariable(min = $min, min = $max)"


object IntIoVariable:
  def fromProperties[F[_] : MonadThrow](properties: Map[String, Value]): F[IntIoVariable[F]] =
    for
      min <- properties.getNumber[F, Int]("min")
      max <- properties.getNumber[F, Int]("max")
    yield IntIoVariable(min, max)


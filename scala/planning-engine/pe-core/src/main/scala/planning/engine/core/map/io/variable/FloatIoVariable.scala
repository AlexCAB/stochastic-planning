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


class  FloatIoVariable[F[_] : ApplicativeThrow](min: Float, max: Float) extends IoVariable[F, Float]:
  private val scalingConstant = 10000.0

  override def valueForIndex(index: Index): F[Float] = index.value / scalingConstant match
    case v if v >= min && v <= max => v.toFloat.pure
    case v => s"Value $v of index $index not in range: $min to $max".assertionError

  override def indexForValue(value: Float): F[Index] = value match
    case v if v >= min && v <= max => Index((v * scalingConstant).toLong).pure
    case v => s"Value $v not in range: $min to $max".assertionError

  override def toProperties: F[Map[String, Value]] = propsOf(
    "type" -> Value.Str("float"),
    "min" -> Value.Decimal(min),
    "max" -> Value.Decimal(max))

  override def toString: String = s"FloatIoVariable($min, $max)"


object FloatIoVariable:
  def fromProperties[F[_] : MonadThrow](properties: Map[String, Value]): F[FloatIoVariable[F]] =
    for
      min <- properties.getNumber[F, Float]("min")
      max <- properties.getNumber[F, Float]("max")
    yield FloatIoVariable(min, max)


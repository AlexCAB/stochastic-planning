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

package planning.engine.map.io.variable

import cats.MonadThrow
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import planning.engine.common.values.Index
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.map.io.variable.IoVariable.*

final case class FloatIoVariable[F[_]: MonadThrow](min: Double, max: Double) extends IoVariable[F, Double]:
  private val scalingConstant = 10000.0

  override def valueForIndex(index: Index): F[Double] = index.value / scalingConstant match
    case v if v >= min && v <= max => v.pure
    case v                         => s"Value $v of index $index not in range: $min to $max".assertionError

  override def indexForValue(value: Double): F[Index] = value match
    case v if v >= min && v <= max => Index((v * scalingConstant).toLong).pure
    case v                         => s"Value $v not in range: $min to $max".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    VAR_TYPE_PROP_NAME -> "float".toDbParam,
    MIN_PROP_NAME -> min.toDbParam,
    MAX_PROP_NAME -> max.toDbParam
  )

  override def toString: String = s"FloatIoVariable(min = $min, min = $max)"

object FloatIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[FloatIoVariable[F]] =
    for
      min <- properties.getValue[F, Double](MIN_PROP_NAME)
      max <- properties.getValue[F, Double](MAX_PROP_NAME)
    yield FloatIoVariable(min, max)

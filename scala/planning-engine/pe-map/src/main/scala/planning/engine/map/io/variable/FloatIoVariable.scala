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
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.*

final case class FloatIoVariable[F[_]: MonadThrow](min: Double, max: Double) extends IoVariable[F, Double]:
  private val scalingConstant = 10000.0

  override def valueForIndex(index: IoIndex): F[Double] = index.value / scalingConstant match
    case v if v >= min && v <= max => v.pure
    case v                         => s"Value $v of index $index not in range: $min to $max".assertionError

  override def indexForValue(value: Double): F[IoIndex] = value match
    case v if v >= min && v <= max => IoIndex((v * scalingConstant).toLong).pure
    case v                         => s"Value $v not in range: $min to $max".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.VAR_TYPE -> PROP_VALUE.FLOAT_TYPE.toDbParam,
    PROP_NAME.MIN -> min.toDbParam,
    PROP_NAME.MAX -> max.toDbParam
  )

  override def toString: String = s"FloatIoVariable(min = $min, min = $max)"

object FloatIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[FloatIoVariable[F]] =
    for
      min <- properties.getValue[F, Double](PROP_NAME.MIN)
      max <- properties.getValue[F, Double](PROP_NAME.MAX)
    yield FloatIoVariable(min, max)

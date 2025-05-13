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
import planning.engine.common.values.IoValueIndex
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.map.io.variable.IoVariable.BOOL_TYPE_NAME

final case class BooleanIoVariable[F[_]: MonadThrow](acceptableValues: Set[Boolean]) extends IoVariable[F, Boolean]:
  override def valueForIndex(index: IoValueIndex): F[Boolean] = index match
    case IoValueIndex(0) if acceptableValues.contains(false) => false.pure
    case IoValueIndex(1) if acceptableValues.contains(true)  => true.pure
    case _ => s"Invalid index ($index) or not in acceptable values: $acceptableValues".assertionError

  override def indexForValue(value: Boolean): F[IoValueIndex] =
    if acceptableValues.contains(value) then IoValueIndex(if value then 1 else 0).pure
    else s"Value '$value' not in acceptable values: $acceptableValues".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.VAR_TYPE -> BOOL_TYPE_NAME.toDbParam,
    PROP_NAME.DOMAIN -> acceptableValues.toList.toDbParam
  )

  override def toString: String = s"BooleanIoVariable(acceptableValues = [${acceptableValues.mkString(", ")}])"

object BooleanIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[BooleanIoVariable[F]] =
    properties.getList[F, Boolean](PROP_NAME.DOMAIN).map(bs => BooleanIoVariable(bs.toSet))

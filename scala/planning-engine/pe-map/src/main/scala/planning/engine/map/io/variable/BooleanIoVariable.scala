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
import planning.engine.common.values.io.IoIndex
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.BOOL_TYPE

abstract class BooleanIoVariableLike[F[_]: MonadThrow] extends IoVariable[F, Boolean]:
  def acceptableValues: Set[Boolean]
  def valueForIndex(index: IoIndex): F[Boolean]
  def indexForValue(value: Boolean): F[IoIndex]

final case class BooleanIoVariable[F[_]: MonadThrow](acceptableValues: Set[Boolean]) extends BooleanIoVariableLike[F]:

  override def valueForIndex(index: IoIndex): F[Boolean] = index match
    case IoIndex(0) if acceptableValues.contains(false) => false.pure
    case IoIndex(1) if acceptableValues.contains(true)  => true.pure
    case _ => s"Invalid index ($index) or not in acceptable values: $acceptableValues".assertionError

  override def indexForValue(value: Boolean): F[IoIndex] =
    if acceptableValues.contains(value) then IoIndex(if value then 1 else 0).pure
    else s"Value '$value' not in acceptable values: $acceptableValues".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP.VAR_TYPE -> BOOL_TYPE.toDbParam,
    PROP.DOMAIN -> acceptableValues.toList.toDbParam
  )

  override lazy val toString: String = s"BooleanIoVariable(acceptableValues = [${acceptableValues.mkString(", ")}])"

object BooleanIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[BooleanIoVariable[F]] =
    properties.getList[F, Boolean](PROP.DOMAIN).map(bs => BooleanIoVariable(bs.toSet))

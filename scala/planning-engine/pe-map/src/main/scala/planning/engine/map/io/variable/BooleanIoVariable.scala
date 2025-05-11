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

final case class BooleanIoVariable[F[_]: MonadThrow](acceptableValues: Set[Boolean]) extends IoVariable[F, Boolean]:
  override def valueForIndex(index: Index): F[Boolean] = index match
    case Index(0) if acceptableValues.contains(false) => false.pure
    case Index(1) if acceptableValues.contains(true)  => true.pure
    case _ => s"Invalid index ($index) or not in acceptable values: $acceptableValues".assertionError

  override def indexForValue(value: Boolean): F[Index] =
    if acceptableValues.contains(value) then Index(if value then 1 else 0).pure
    else s"Value '$value' not in acceptable values: $acceptableValues".assertionError

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    VAR_TYPE_PROP_NAME -> BOOL_TYPE_NAME.toDbParam,
    DOMAIN_PROP_NAME -> acceptableValues.toList.toDbParam
  )

  override def toString: String = s"BooleanIoVariable(acceptableValues = [${acceptableValues.mkString(", ")}])"

object BooleanIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[BooleanIoVariable[F]] =
    properties.getList[F, Boolean](DOMAIN_PROP_NAME).map(bs => BooleanIoVariable(bs.toSet))

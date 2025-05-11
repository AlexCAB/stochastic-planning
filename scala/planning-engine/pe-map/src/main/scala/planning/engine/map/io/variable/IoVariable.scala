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
| created: 2025-03-18 |||||||||||*/

package planning.engine.map.io.variable

import cats.MonadThrow
import cats.syntax.all.*
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.common.values.Index

trait IoVariable[F[_], T]:
  def valueForIndex(index: Index): F[T]
  def indexForValue(value: T): F[Index]
  def toQueryParams: F[Map[String, Param]]

object IoVariable:

  val VAR_TYPE_PROP_NAME = "var_type"
  val DOMAIN_PROP_NAME = "domain"
  val MIN_PROP_NAME = "min"
  val MAX_PROP_NAME = "max"

  val BOOL_TYPE_NAME = "bool"
  val INT_TYPE_NAME = "int"
  val FLOAT_TYPE_NAME = "float"
  val LIST_STR_TYPE_NAME = "list-str"

  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[IoVariable[F, ?]] =
    properties.get(VAR_TYPE_PROP_NAME) match
      case Some(Value.Str(BOOL_TYPE_NAME)) =>
        BooleanIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(INT_TYPE_NAME)) =>
        IntIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(FLOAT_TYPE_NAME)) =>
        FloatIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(LIST_STR_TYPE_NAME)) =>
        ListStrIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(t) => s"Invalid variable type: $t".assertionError
      case None    => "Missing 'type' property for variable".assertionError

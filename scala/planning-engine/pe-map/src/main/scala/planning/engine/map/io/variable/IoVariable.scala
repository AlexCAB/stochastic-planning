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
import planning.engine.common.properties.PROP
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*

trait IoVariable[F[_], T]:
  def valueForIndex(index: IoIndex): F[T]
  def indexForValue(value: T): F[IoIndex]
  def toQueryParams: F[Map[String, Param]]

object IoVariable:
  object PROP_VALUE:
    val BOOL_TYPE = "bool"
    val INT_TYPE = "int"
    val FLOAT_TYPE = "float"
    val LIST_STR_TYPE = "list-str"

  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[IoVariable[F, ?]] =
    properties.get(PROP.VAR_TYPE) match
      case Some(Value.Str(BOOL_TYPE)) =>
        BooleanIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(INT_TYPE)) => IntIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(FLOAT_TYPE)) =>
        FloatIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str(LIST_STR_TYPE)) =>
        ListStrIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(t) => s"Invalid variable type: $t".assertionError
      case None    => "Missing 'type' property for variable".assertionError

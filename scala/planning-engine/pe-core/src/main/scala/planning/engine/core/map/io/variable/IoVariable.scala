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

package planning.engine.core.map.io.variable

import cats.ApplicativeThrow
import cats.MonadThrow
import cats.syntax.all.*
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import planning.engine.common.values.Index

trait IoVariable[F[_], T]:
  def valueForIndex(index: Index): F[T]
  def indexForValue(value: T): F[Index]
  def toProperties: F[Map[String, Value]]

object IoVariable:

  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[IoVariable[F, ?]] =
    properties.get("type") match
      case Some(Value.Str("bool")) => BooleanIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str("int")) => IntIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str("float")) => FloatIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(Value.Str("list-str")) =>
        ListStrIoVariable.fromProperties(properties).map(_.asInstanceOf[IoVariable[F, ?]])

      case Some(t) => s"Invalid variable type: $t".assertionError
      case None    => "Missing 'type' property for variable".assertionError

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

import cats.ApplicativeThrow
import cats.MonadThrow
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import planning.engine.common.values.Index

import planning.engine.common.properties.*
import cats.syntax.all.*

final case class BooleanIoVariable[F[_]: MonadThrow](acceptableValues: Set[Boolean]) extends IoVariable[F, Boolean]:
  override def valueForIndex(index: Index): F[Boolean] = index match
    case Index(0) if acceptableValues.contains(false) => false.pure
    case Index(1) if acceptableValues.contains(true)  => true.pure
    case _ => s"Invalid index ($index) or not in acceptable values: $acceptableValues".assertionError

  override def indexForValue(value: Boolean): F[Index] =
    if acceptableValues.contains(value) then Index(if value then 1 else 0).pure
    else s"Value '$value' not in acceptable values: $acceptableValues".assertionError

  override def toProperties: F[Map[String, Value]] = propsOf(
    "type" -> Value.Str("bool"),
    "domain" -> Value.ListValue(acceptableValues.map(Value.Bool.apply).toList)
  )

  override def toString: String = s"BooleanIoVariable(acceptableValues = [${acceptableValues.mkString(", ")}])"

object BooleanIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[BooleanIoVariable[F]] =
    properties.getList[F, Boolean]("domain").map(bs => BooleanIoVariable(bs.toSet))

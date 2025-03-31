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
import cats.syntax.all.*
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import planning.engine.common.values.Index

class ListStrIoVariable[F[_]: MonadThrow](val elements: Vector[String]) extends IoVariable[F, String]:
  override def valueForIndex(index: Index): F[String] =
    if elements.isDefinedAt(index.value.toInt) then elements(index.value.toInt).pure
    else s"Index $index out of bounds for list of size ${elements.size}".assertionError

  override def indexForValue(value: String): F[Index] = elements.indexOf(value) match
    case -1 => s"Value $value not in list: $elements".assertionError
    case i  => Index(i).pure

  override def toProperties: F[Map[String, Value]] = propsOf(
    "type" -> Value.Str("list-str"),
    "domain" -> Value.ListValue(elements.map(Value.Str.apply).toList)
  )

  override def toString: String = s"ListStrIoVariable(elements = [${elements.mkString(", ")}])"

object ListStrIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[ListStrIoVariable[F]] =
    properties.getList[F, String]("domain").map(es => ListStrIoVariable(es.toVector))

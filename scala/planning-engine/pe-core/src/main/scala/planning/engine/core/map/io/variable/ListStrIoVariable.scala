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

import cats.{ApplicativeThrow, MonadThrow}
import neotypes.model.types.Value
import planning.engine.common.values.Index
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import cats.syntax.all.*


class ListStrIoVariable[F[_] : ApplicativeThrow](elements: Vector[String]) extends IoVariable[F, String]:
  override def valueForIndex(index: Index): F[String] =
    if(elements.size < index.value && elements.isDefinedAt(index.value.toInt)) elements(index.value.toInt).pure
    else s"Index $index out of bounds for list of size ${elements.size}".assertionError

  override def indexForValue(value: String): F[Index] =
    elements.indexOf(value) match
      case -1 => s"Value $value not in list: $elements".assertionError
      case i => Index(i).pure

  override def toProperties: F[Map[String, Value]] = propsOf(
    "type" -> Value.Str("list-str"),
    "domain" -> Value.ListValue(elements.map(Value.Str.apply).toList))

  override def toString: String = s"ListStrIoVariable($elements)"


object ListStrIoVariable:
  def fromProperties[F[_] : MonadThrow](properties: Map[String, Value]): F[ListStrIoVariable[F]] =
    for
      elements <- properties.getList("domain"):
        case Value.Str(b)=> b.pure
        case v => s"Invalid value in 'domain' property: $v".assertionError
    yield ListStrIoVariable(elements.toVector)

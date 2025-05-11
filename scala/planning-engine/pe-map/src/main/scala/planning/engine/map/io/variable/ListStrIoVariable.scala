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

final case class ListStrIoVariable[F[_]: MonadThrow](elements: Vector[String]) extends IoVariable[F, String]:
  override def valueForIndex(index: Index): F[String] =
    if elements.isDefinedAt(index.value.toInt) then elements(index.value.toInt).pure
    else s"Index $index out of bounds for list of size ${elements.size}".assertionError

  override def indexForValue(value: String): F[Index] = elements.indexOf(value) match
    case -1 => s"Value $value not in list: $elements".assertionError
    case i  => Index(i).pure

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    VAR_TYPE_PROP_NAME -> "list-str".toDbParam,
    DOMAIN_PROP_NAME -> elements.toList.toDbParam
  )

  override def toString: String = s"ListStrIoVariable(elements = [${elements.mkString(", ")}])"

object ListStrIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[ListStrIoVariable[F]] =
    properties.getList[F, String](DOMAIN_PROP_NAME).map(es => ListStrIoVariable(es.toVector))

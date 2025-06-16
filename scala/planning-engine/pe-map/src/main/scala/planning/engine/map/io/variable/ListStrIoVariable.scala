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
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.*

final case class ListStrIoVariable[F[_]: MonadThrow](elements: List[String]) extends IoVariable[F, String]:
  override def valueForIndex(index: IoIndex): F[String] =
    if elements.isDefinedAt(index.value.toInt) then elements(index.value.toInt).pure
    else s"Index $index out of bounds for list of size ${elements.size}".assertionError

  override def indexForValue(value: String): F[IoIndex] = elements.indexOf(value) match
    case -1 => s"Value $value not in list: $elements".assertionError
    case i  => IoIndex(i).pure

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP.VAR_TYPE -> PROP_VALUE.LIST_STR_TYPE.toDbParam,
    PROP.DOMAIN -> elements.toList.toDbParam
  )

  override def toString: String = s"ListStrIoVariable(elements = [${elements.mkString(", ")}])"

object ListStrIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[ListStrIoVariable[F]] =
    properties.getList[F, String](PROP.DOMAIN).map(es => ListStrIoVariable(es.toList))

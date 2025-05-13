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
import planning.engine.common.values.node.io.IoValueIndex
import planning.engine.map.io.variable.IoVariable.*

final case class ListStrIoVariable[F[_]: MonadThrow](elements: Vector[String]) extends IoVariable[F, String]:
  override def valueForIndex(index: IoValueIndex): F[String] =
    if elements.isDefinedAt(index.value.toInt) then elements(index.value.toInt).pure
    else s"Index $index out of bounds for list of size ${elements.size}".assertionError

  override def indexForValue(value: String): F[IoValueIndex] = elements.indexOf(value) match
    case -1 => s"Value $value not in list: $elements".assertionError
    case i  => IoValueIndex(i).pure

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.VAR_TYPE -> LIST_STR_TYPE_NAME.toDbParam,
    PROP_NAME.DOMAIN -> elements.toList.toDbParam
  )

  override def toString: String = s"ListStrIoVariable(elements = [${elements.mkString(", ")}])"

object ListStrIoVariable:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[ListStrIoVariable[F]] =
    properties.getList[F, String](PROP_NAME.DOMAIN).map(es => ListStrIoVariable(es.toVector))

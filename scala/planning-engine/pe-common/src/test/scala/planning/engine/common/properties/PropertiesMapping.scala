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
| created: 2025-05-08 |||||||||||*/

package planning.engine.common.properties

import neotypes.model.query.QueryParam
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param

object PropertiesMapping:

  extension (value: Value)
    def toQueryParam: QueryParam = value match
      case Value.Str(s)       => QueryParam(s)
      case Value.Integer(i)   => QueryParam(i)
      case Value.Decimal(f)   => QueryParam(f)
      case Value.Bool(b)      => QueryParam(b)
      case Value.ListValue(l) => QueryParam(l.map(_.toQueryParam))
      case _                  => throw new IllegalArgumentException(s"Unsupported value type: $value")

    def toParam: Param = Param(toQueryParam)

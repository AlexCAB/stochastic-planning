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


package planning.engine.common.properties

import cats.{Applicative, ApplicativeThrow}
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

import scala.reflect.Typeable

package object properties


inline def propsOf[M[_] : Applicative](items: (String, Value)*): M[Map[String, Value]] = items.toMap.pure


extension (properties: Map[String, Value])
  inline def getForKey[M[_] : ApplicativeThrow, V](key: String)(parse: Value => M[V]): M[V] =
    properties.get(key) match
      case Some(v: Value) => parse(v)
      case _ => s"Missing property '$key' in properties: $properties".assertionError

  inline def getList[M[_] : ApplicativeThrow, V](key: String)(parse: Value => M[V]): M[List[V]] =
    getForKey(key):
      case Value.ListValue(values) => values.map(parse).sequence
      case v => s"Expected a list value, but got: $v".assertionError
  
  inline def getNumber[M[_] : ApplicativeThrow, V <: (Int | Float) : Typeable](key: String): M[V] =
    getForKey(key):
      case Value.Integer(long) if long <= Int.MaxValue && long >= Int.MinValue => 
        long.toInt match
          case int: V => int.pure
          case _ => s"Unexpected a int value, for kay: $key".assertionError

      case Value.Decimal(double) if double <= Float.MaxValue && double >= Float.MinValue => 
        double.toFloat match
          case float: V => float.pure
          case _ => s"Expected a float value, for kay: $key".assertionError

      case v => s"Expected a int value, but got: $v".assertionError


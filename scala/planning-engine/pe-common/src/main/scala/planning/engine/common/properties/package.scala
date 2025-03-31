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

import cats.Monad
import cats.ApplicativeThrow
import cats.syntax.all.*
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError

import scala.reflect.Typeable

package object properties

inline def propsOf[F[_]: Monad](items: (String, Value | F[Value] | F[Map[String, Value]])*): F[Map[String, Value]] =
  items
    .map:
      case (key, value: Value) => Map(key -> value).pure
      case (key, value: F[?] @unchecked) => value.map:
          case v: Value => Map(key -> v)
          case m: Map[String, Value] @unchecked => m.map:
              case (k, v) => s"$key.$k" -> v
    .reduce((ma, mb) => ma.flatMap(a => mb.map(b => b ++ a)))

extension (properties: Map[String, Value])
  private def validateValue[M[_]: ApplicativeThrow, I, V <: Int | Float | String | Boolean: Typeable](
      in: I,
      key: String
  ): M[V] = in match
    case v: V => v.pure
    case v    => s"Type of value '$v' not match expected, for kay: $key".assertionError

  private def valueFor[M[_]: ApplicativeThrow, V <: (Int | Float | String | Boolean): Typeable](
      value: Value,
      key: String
  ): M[V] = value match
    case Value.Integer(l) if l <= Int.MaxValue && l >= Int.MinValue     => validateValue[M, Int, V](l.toInt, key)
    case Value.Decimal(d) if d <= Float.MaxValue && d >= Float.MinValue => validateValue[M, Float, V](d.toFloat, key)
    case Value.Str(str)                                                 => validateValue[M, String, V](str, key)
    case Value.Bool(bool)                                               => validateValue[M, Boolean, V](bool, key)
    case v => s"Expected a Int, Float, String or Boolean value, but got: $v".assertionError

  private def parseValue[M[_]: ApplicativeThrow, V](key: String)(parse: Value => M[V]): M[V] = properties.get(key) match
    case Some(v: Value) => parse(v)
    case _              => s"Missing property '$key' in properties: $properties".assertionError

  private def parseList[M[_]: ApplicativeThrow, V](key: String)(parse: Value => M[V]): M[List[V]] = parseValue(key):
    case Value.ListValue(values) => values.map(parse).sequence
    case v                       => s"Expected a list value, but got: $v".assertionError

  inline def getValue[M[_]: ApplicativeThrow, V <: Int | Float | String | Boolean: Typeable](key: String): M[V] =
    parseValue(key)(v => valueFor(v, key))

  inline def getList[M[_]: ApplicativeThrow, V <: Int | Float | String | Boolean: Typeable](key: String): M[List[V]] =
    parseList(key)(v => valueFor(v, key))

  inline def getProps[M[_]: ApplicativeThrow](key: String): M[Map[String, Value]] =
    val prefix = key + "."
    properties.filter((k, _) => k.startsWith(prefix)).map((k, v) => (k.substring(prefix.length), v)).pure

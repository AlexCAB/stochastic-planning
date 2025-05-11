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

import cats.MonadThrow
import neotypes.model.types.Value
import planning.engine.common.errors.assertionError

import scala.reflect.Typeable
import cats.syntax.all.*
import neotypes.mappers.ParameterMapper
import neotypes.model.query.QueryParam.NullValue
import neotypes.query.QueryArg.Param

package object properties

type ScalaValue = Long | Double | String | Boolean

extension [T](value: T)
  def toDbParam(implicit mapper: ParameterMapper[T]): Param =
    Param(if value == null then NullValue else mapper.toQueryParam(value))

extension [F[_]: MonadThrow, P <: Param | Value](parms: F[Map[String, P]])
  inline def addKeyPrefix(prefix: String): F[Map[String, P]] = parms.map(_.map((k, v) => (s"$prefix.$k", v)))

  inline def removeKeyPrefix(prefix: String): F[Map[String, P]] = parms.flatMap(_
    .map:
      case (k, v) if k.startsWith(prefix + ".") => (k.substring(prefix.length + 1), v).pure
      case (k, _)                               => s"Expected key $k to start with prefix $prefix".assertionError
    .toSeq.sequence
    .map(_.toMap))

inline def paramsOf[F[_]: MonadThrow](items: (
    String,
    Param | Option[Param] | F[Param] | F[Map[String, Param]]
)*): F[Map[String, Param]] = items
  .map:
    case (key, value: Param)                    => Map(key -> value).pure
    case (key, value: Option[Param] @unchecked) => value.map(v => Map(key -> v)).getOrElse(Map.empty).pure
    case (key, value: F[?] @unchecked) => value.flatMap:
        case v: Param                         => Map(key -> v).pure
        case m: Map[String, Param] @unchecked => m.pure.addKeyPrefix(key)
  .reduce((ma, mb) => ma.flatMap(a => mb.map(b => b ++ a)))

extension (propsMap: Map[String, Value])
  private def validateValue[F[_]: MonadThrow, I, V <: ScalaValue: Typeable](
      in: I,
      key: String
  ): F[V] = in match
    case v: V => v.pure
    case v    => s"Type of value '$v' not match expected, for kay: $key".assertionError

  private def valueFor[F[_]: MonadThrow, V <: ScalaValue: Typeable](
      value: Value,
      key: String
  ): F[V] = value match
    case Value.Integer(l) => validateValue[F, Long, V](l, key)
    case Value.Decimal(d) => validateValue[F, Double, V](d, key)
    case Value.Str(str)   => validateValue[F, String, V](str, key)
    case Value.Bool(bool) => validateValue[F, Boolean, V](bool, key)
    case v                => s"Expected a Int, Float, String or Boolean value, but got: $v".assertionError

  private def parseValue[F[_]: MonadThrow, V](key: String)(parse: Value => F[V]): F[V] = propsMap.get(key) match
    case Some(v: Value) => parse(v)
    case _              => s"Missing property '$key' in properties: $propsMap".assertionError

  private def parseList[F[_]: MonadThrow, V](key: String)(parse: Value => F[V]): F[List[V]] = parseValue(key):
    case Value.ListValue(values) => values.map(parse).sequence
    case v                       => s"Expected a list value, but got: $v".assertionError

  inline def getValue[F[_]: MonadThrow, V <: ScalaValue: Typeable](key: String): F[V] =
    parseValue(key)(v => valueFor(v, key))

  inline def getOptional[F[_]: MonadThrow, V <: ScalaValue: Typeable](key: String): F[Option[V]] =
    propsMap.get(key) match
      case Some(v: Value) => valueFor(v, key).map(_.some)
      case _              => None.pure

  inline def getList[F[_]: MonadThrow, V <: ScalaValue: Typeable](key: String): F[List[V]] =
    parseList(key)(v => valueFor(v, key))

  inline def getProps[F[_]: MonadThrow](key: String): F[Map[String, Value]] =
    val keyWithDot = key + "."
    propsMap.filter((k, _) => k.startsWith(keyWithDot)).pure.removeKeyPrefix(key)

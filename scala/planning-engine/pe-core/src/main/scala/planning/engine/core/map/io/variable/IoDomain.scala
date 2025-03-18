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
| created: 2025-03-18 |||||||||||*/


package planning.engine.core.map.io.variable

import cats.{Applicative, ApplicativeThrow, MonadThrow}
import neotypes.model.types.Value
import planning.engine.common.values.Index
import cats.syntax.all.*


trait IoDomain[F[_], T]:
  def valueForIndex(index: Index): F[T]
  def indexForValue(value: T): F[Index]
  def to_properties: F[Map[String, Value]]


class BooleanIoDomain[F[_] : ApplicativeThrow](acceptableValues: Set[Boolean]) extends IoDomain[F, Boolean]:
  override def valueForIndex(index: Index): F[Boolean] = index match
    case Index(0) if acceptableValues.contains(true) => Applicative[F].pure(false)
    case Index(1) if acceptableValues.contains(false) => Applicative[F].pure(true)
    
    case _ => ApplicativeThrow[F].raiseError(
      new IllegalArgumentException(s"Invalid index ($index) or not in acceptable values: $acceptableValues"))

  override def indexForValue(value: Boolean): F[Index] =
    if (acceptableValues.contains(value)) Applicative[F].pure(Index(if(value) 1 else 0))
    else ApplicativeThrow[F].raiseError(
        new IllegalArgumentException(s"Value '$value' not in acceptable values: $acceptableValues"))

  override def to_properties: F[Map[String, Value]] = Applicative[F].pure(Map(
    "type" -> Value.Str("bool"),
    "domain" -> Value.ListValue(acceptableValues.map(Value.Bool.apply).toList)
  ))


class IntIoDomain[F[_] : MonadThrow](min: Int, max: Int) extends IoDomain[F, Int]:
  private def index2Int(index: Index): F[Int] = index.value match
    case i if i <= Int.MaxValue && i >= 0 => Applicative[F].pure(i.toInt)
    
    case i if i > Int.MaxValue => -1 * (i - Int.MaxValue) match
      case ni if ni < Int.MinValue => Applicative[F].pure(ni.toInt)
      case _ => ApplicativeThrow[F].raiseError(
        new IllegalArgumentException(s"Index $index out of Int range for negative values"))

    case _ => ApplicativeThrow[F].raiseError(
      new IllegalArgumentException(s"Index $index is < 0"))

  override def valueForIndex(index: Index): F[Int] = index2Int(index).flatMap:
    case v if v >= min && v <= max => Applicative[F].pure(v)
    case v => ApplicativeThrow[F].raiseError(
      new IllegalArgumentException(s"Int $v of index $index not in range: $min to $max"))
  
  override def indexForValue(value: Int): F[Index] = value match
    case v if v >= 0 && v >= min && v <= max => Applicative[F].pure(Index(v))
    case v if v < 0 && v >= min && v <= max => Applicative[F].pure(Index(Int.MaxValue + (v * -1)))
    case _ => ApplicativeThrow[F].raiseError(
      new IllegalArgumentException(s"Value $value not in range: $min to $max"))

  override def to_properties: F[Map[String, Value]] = Applicative[F].pure(Map(
    "type" -> Value.Str("int"),
    "min" -> Value.Integer(min),
    "max" -> Value.Integer(max)
  ))

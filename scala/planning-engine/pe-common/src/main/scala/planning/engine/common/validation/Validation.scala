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
| created: 2025-06-28 |||||||||||*/

package planning.engine.common.validation

import cats.ApplicativeThrow

import scala.collection.Seq

trait Validation:
  def validationName: String
  def validationErrors: List[Throwable]

  protected def validations(validators: (Boolean, String)*): List[Throwable] = validators.toList
    .flatMap((isValid, errMsg) => if isValid then Nil else List(new IllegalArgumentException(errMsg)))

  extension [C[_] <: Seq[?]](seq: C[Any])
    protected inline def isDistinct(msg: String): (Boolean, String) =
      val duplicates = seq.groupBy(identity).filter((_, v) => v.size > 1).values.flatten.toSet
      (duplicates.isEmpty, s"$msg, duplicates: ${duplicates.mkString(", ")}")

    protected inline def containsAllOf[O[_] <: Seq[?]](others: O[Any], msg: String): (Boolean, String) =
      val set = seq.toSet
      val dif = others.filterNot(set.contains)
      (dif.isEmpty, s"$msg, missing elements: ${dif.mkString(", ")}")

    protected inline def haveSameElems[O[_] <: Seq[?]](others: O[Any], msg: String): (Boolean, String) =
      val thisSet = seq.toSet
      val otherSet = others.toSet
      val dif = (seq ++ others).filterNot(e => thisSet.contains(e) && otherSet.contains(e))
      (dif.isEmpty, s"$msg, not same elements: ${dif.mkString(", ")}")

object Validation:
  def validate[F[_]: ApplicativeThrow](obj: Validation): F[Unit] =
    if obj.validationErrors.isEmpty then ApplicativeThrow[F].unit
    else ApplicativeThrow[F].raiseError(ValidationError(obj.validationName, obj.validationErrors))

  def validateList[F[_]: ApplicativeThrow](objects: Iterable[Validation]): F[Unit] =
    val (names, errors) = objects.foldRight(List[String](), List[Throwable]()):
      case (obj, (accNames, accErrors)) =>
        val errors = obj.validationErrors

        if errors.isEmpty then (accNames, accErrors)
        else (obj.validationName :: accNames, errors ++ accErrors)

    if errors.isEmpty then ApplicativeThrow[F].unit
    else ApplicativeThrow[F].raiseError(ValidationError(names.mkString(", "), errors))

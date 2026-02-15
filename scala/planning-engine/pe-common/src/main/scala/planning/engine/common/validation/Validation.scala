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

trait Validation:
  def validations: (String, List[Throwable])

  protected def validate(name: String)(validators: (Boolean, String)*): (String, List[Throwable]) =
    val errors = validators
      .toList
      .flatMap((isValid, errMsg) => if isValid then Nil else List(new IllegalArgumentException(errMsg)))
    name -> errors

  protected def validate(
      name: String,
      prev: (String, List[Throwable])
  )(validators: (Boolean, String)*): (String, List[Throwable]) =
    val (curName, curErrors) = validate(name)(validators*)
    val (prevName, prevErrors) = prev
    (curName + "/" + prevName, curErrors ++ prevErrors)

  extension [C[_] <: Iterable[?]](seq: C[Any])
    protected inline def isDistinct(msg: String): (Boolean, String) =
      val duplicates = seq.groupBy(identity).filter((_, v) => v.size > 1).values.flatten.toSet
      (duplicates.isEmpty, s"$msg, duplicates: ${duplicates.mkString(", ")}")

    protected inline def containsAllOf[O[_] <: Iterable[?]](others: O[Any], msg: String): (Boolean, String) =
      val set = seq.toSet
      val dif = others.filterNot(set.contains).toSet
      (dif.isEmpty, s"$msg, missing elements: ${dif.mkString(", ")}")

    protected inline def haveSameElems[O[_] <: Iterable[?]](others: O[Any], msg: String): (Boolean, String) =
      val thisSet = seq.toSet
      val otherSet = others.toSet
      val dif = (seq ++ others).filterNot(e => thisSet.contains(e) && otherSet.contains(e))
      (dif.isEmpty, s"$msg, have not same elements: ${dif.mkString(", ")}")

    protected inline def haveDifferentElems[O[_] <: Iterable[?]](others: O[Any], msg: String): (Boolean, String) =
      val thisSet = seq.toSet
      val otherSet = others.toSet
      val int = thisSet.intersect(otherSet)
      (int.isEmpty, s"$msg, have same elements: ${int.mkString(", ")}")

  extension (seq: Iterable[(Any, Any)])
    protected inline def allEquals(msg: String): (Boolean, String) =
      val notEq = seq.filter(e => e._1 != e._2)
      (notEq.isEmpty, s"$msg, have equal elements: ${notEq.mkString(", ")}")

object Validation:
  def validate[F[_]: ApplicativeThrow](obj: Validation): F[Unit] =
    val (name, errors) = obj.validations
    if errors.isEmpty then ApplicativeThrow[F].unit
    else ApplicativeThrow[F].raiseError(ValidationError(name, errors))

  def validateList[F[_]: ApplicativeThrow](objects: Iterable[Validation]): F[Unit] =
    val (names, errors) = objects.foldRight(List[String](), List[Throwable]()):
      case (obj, (accNames, accErrors)) =>
        val (name, errors) = obj.validations

        if errors.isEmpty then (accNames, accErrors)
        else (name :: accNames, errors ++ accErrors)

    if errors.isEmpty then ApplicativeThrow[F].unit
    else ApplicativeThrow[F].raiseError(ValidationError(names.mkString(", "), errors))

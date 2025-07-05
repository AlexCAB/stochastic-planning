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
  def validationName: String
  def validationErrors: List[Throwable]

  protected def validations(validators: (Boolean, String)*): List[Throwable] = validators.toList
    .flatMap((isValid, errMsg) => if isValid then Nil else List(new IllegalArgumentException(errMsg)))

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

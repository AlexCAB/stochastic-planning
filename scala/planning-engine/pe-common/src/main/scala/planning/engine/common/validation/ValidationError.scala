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

final case class ValidationError(objName: String, errors: List[Throwable]) extends Exception(
      s"Validation failed for $objName: ${errors.map(_.getMessage).mkString(", ")}",
      errors.headOption.orNull
    )

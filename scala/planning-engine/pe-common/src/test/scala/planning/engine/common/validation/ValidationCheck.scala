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
| created: 2025-12-21 |||||||||||*/

package planning.engine.common.validation

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import planning.engine.common.SpecLogging

trait ValidationCheck:
  self: AsyncIOSpec & Matchers & SpecLogging =>

  extension [V <: Validation](obj: V)
    def checkValidationName(nameInclude: String, tn: String = "checkValidationName"): IO[Assertion] =
      obj.pure[IO].map(_.validationName)
        .logValue(tn, s"nameInclude = $nameInclude")
        .asserting(_ must include(nameInclude))

    def checkNoValidationError(tn: String = "checkNoValidationError"): IO[Assertion] = obj.validationErrors.pure[IO]
      .logValue(tn)
      .asserting(_ mustBe empty)

    def checkOneValidationError(msgInclude: String, tn: String = "checkOneValidationError"): IO[Assertion] =
      obj.pure[IO].map(_.validationErrors)
        .logValue(tn, s"msgInclude = $msgInclude")
        .asserting: errs =>
          if errs.size != 1 then fail(s"Expected exactly one validation error, but found $errs")
          errs.head.getMessage must include(msgInclude)

    def checkOneOfValidationErrors(
        msgInclude: String,
        tn: String = "checkOneOfValidationErrors"
    ): IO[Assertion] = obj.pure[IO].map(_.validationErrors)
      .logValue(tn, s"msgInclude = $msgInclude")
      .asserting: errs =>
        errs.find(_.getMessage.contains(msgInclude)) match
          case None    => fail(s"No validation error includes message: $msgInclude")
          case Some(_) => succeed

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
| created: 2025-03-21 |||||||||||*/

package planning.engine.common.errors

import cats.effect.IO
import planning.engine.common.UnitSpecIO

class ErrorsSpec extends UnitSpecIO:

  "assertionError" should:
    "raise an AssertionError with the given message" in: _ =>
      val errorMessage = "This is an assertion error"
      errorMessage.assertionError[IO, Unit].assertThrowsWithMessage[AssertionError](errorMessage)

  "assertDistinct" should:
    "return the same sequence if all elements are distinct" in: _ =>
      val distinctSeq = List(1, 2, 3, 4, 5)
      distinctSeq.assertDistinct[IO]("Elements are not distinct").asserting(_ mustEqual distinctSeq)

    "raise an AssertionError if the sequence contains duplicates" in: _ =>
      val duplicateSeq = List(1, 2, 2, 3, 4)
      duplicateSeq.assertDistinct[IO](
        "Elements are not distinct"
      ).assertThrowsWithMessage[AssertionError]("Elements are not distinct, seq: 1,2,2,3,4")

  "assertSameSize" should:
    "return the input tuple when both collections have the same size" in: tn =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b", "c")
      (left, right).assertSameSize[IO]("Collections must have the same size")
        .logValue(tn)
        .asserting(_ mustEqual (left, right))

    "raise an error when collections have different sizes" in: tn =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b")
      (left, right).assertSameSize[IO]("Collections must have the same size")
        .logValue(tn)
        .assertThrows[AssertionError]

  "assertTrue" should:
    "complete successfully when the condition is true" in: tn =>
      true.assertTrue[IO]("Condition must be true")
        .logValue(tn)
        .asserting(_ mustEqual (()))

    "raise an error when the condition is false" in: tn =>
      false.assertTrue[IO]("Condition must be true")
        .logValue(tn)
        .assertThrows[AssertionError]

  "assetAnNumberOf" should:
    "complete successfully when the condition is true" in: tn =>
      5L.assetAnNumberOf[IO]("Value is not positive")
        .logValue(tn)
        .asserting(_ mustEqual (()))

    "raise an error when the condition is false" in: tn =>
      -1L.assetAnNumberOf[IO]("Value is not positive")
        .logValue(tn)
        .assertThrows[AssertionError]

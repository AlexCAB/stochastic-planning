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
    "raise an AssertionError with the given message" in:
      val errorMessage = "This is an assertion error"
      errorMessage.assertionError[IO, Unit].assertThrowsWithMessage[AssertionError](errorMessage)

  "assertDistinct" should:
    "return the same sequence if all elements are distinct" in:
      val distinctSeq = List(1, 2, 3, 4, 5)
      distinctSeq.assertDistinct[IO]("Elements are not distinct").asserting(_ mustEqual distinctSeq)

    "raise an AssertionError if the sequence contains duplicates" in:
      val duplicateSeq = List(1, 2, 2, 3, 4)
      duplicateSeq.assertDistinct[IO](
        "Elements are not distinct"
      ).assertThrowsWithMessage[AssertionError]("Elements are not distinct, seq: 1,2,2,3,4")

  "assertSameSize" should:
    "return the input tuple when both collections have the same size" in:
      val left = List(1, 2, 3)
      val right = Vector("a", "b", "c")
      (left, right).assertSameSize[IO]("Collections must have the same size")
        .logValue
        .asserting(_ mustEqual (left, right))

    "raise an error when collections have different sizes" in:
      val left = List(1, 2, 3)
      val right = Vector("a", "b")
      (left, right).assertSameSize[IO]("Collections must have the same size")
        .logValue
        .assertThrows[AssertionError]

  "assertTrue" should:
    "complete successfully when the condition is true" in:
      true.assertTrue[IO]("Condition must be true")
        .logValue
        .asserting(_ mustEqual (()))

    "raise an error when the condition is false" in:
      false.assertTrue[IO]("Condition must be true")
        .logValue
        .assertThrows[AssertionError]

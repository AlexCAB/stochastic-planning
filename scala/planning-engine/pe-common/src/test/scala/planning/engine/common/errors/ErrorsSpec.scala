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
      List(1, 2, 3, 4, 5).assertDistinct[IO]("Elements are not distinct").asserting(_ mustEqual distinctSeq)

    "raise an AssertionError if the sequence contains duplicates" in: _ =>
      List(1, 2, 2, 3, 4).assertDistinct[IO](
        "Elements are not distinct"
      ).assertThrowsWithMessage[AssertionError]("Elements are not distinct, seq: 1,2,2,3,4")

  "assertNonEmpty" should:
    "return the sequence when it is not empty" in: _ =>
      val seq = List(1, 2, 3)
      seq.assertNonEmpty[IO]("Sequence is empty").asserting(_ mustEqual seq)

    "raise an error when the sequence is empty" in: _ =>
      List.empty[Int].assertNonEmpty[IO]("Sequence is empty").assertThrows[AssertionError]

  "assertUniform" should:
    "return the sequence when all elements are the same" in: _ =>
      val seq = List(1, 1, 1)
      seq.assertUniform[IO]("Elements are not uniform").asserting(_ mustEqual seq)

    "return the sequence when it is empty" in: _ =>
      val seq = List.empty[Int]
      seq.assertUniform[IO]("Elements are not uniform").asserting(_ mustEqual seq)

    "raise an error when elements are not the same" in: _ =>
      val seq = List(1, 2, 1)
      seq.assertUniform[IO]("Elements are not uniform").assertThrows[AssertionError]

  "assertSameSize" should:
    "return the input tuple when both collections have the same size" in: _ =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b", "c")
      (left, right).assertSameSize[IO]("Collections must have the same size").asserting(_ mustEqual (left, right))

    "raise an error when collections have different sizes" in: _ =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b")
      (left, right).assertSameSize[IO]("Collections must have the same size").assertThrows[AssertionError]

  "assertSameElems" should:
    "return the tuple when both collections have the same elements regardless of order" in: _ =>
      val tuple = (List(1, 2, 3), List(3, 2, 1))
      tuple.assertSameElems[IO]("Collections do not have the same elements").asserting(_ mustEqual tuple)

    "raise an error when collections have different elements" in: _ =>
      (List(1, 2, 3), List(4, 5, 6)).assertSameElems[IO]("Collections do not have the same elements")
        .assertThrows[AssertionError]

  "assertTrue" should:
    "complete successfully when the condition is true" in: _ =>
      true.assertTrue[IO]("Condition must be true").asserting(_ mustEqual (()))

    "raise an error when the condition is false" in: _ =>
      false.assertTrue[IO]("Condition must be true").assertThrows[AssertionError]

  "assetAnNumberOf" should:
    "complete successfully when the condition is true" in: _ =>
      5L.assetAnNumberOf[IO]("Value is not positive").asserting(_ mustEqual (()))

    "raise an error when the condition is false" in: tn =>
      -1L.assetAnNumberOf[IO]("Value is not positive").assertThrows[AssertionError]

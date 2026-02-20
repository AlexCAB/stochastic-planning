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

  "assertTrue" should:
    "complete successfully when the condition is true" in: _ =>
      true.assertTrue[IO]("Condition must be true").assertNoException

    "raise an error when the condition is false" in: _ =>
      false.assertTrue[IO]("Condition must be true").assertThrows[AssertionError]

  "assetAnNumberOf" should:
    "complete successfully when the condition is true" in: _ =>
      5L.assertAnNumberOf[IO]("Value is not positive").assertNoException

    "raise an error when the condition is false" in: tn =>
      -1L.assertAnNumberOf[IO]("Value is not positive").assertThrows[AssertionError]

  "assertPositive" should:
    "complete successfully when the condition is true" in: _ =>
      5L.assertPositive[IO]("Value is not positive").assertNoException

    "raise an error when the condition is false" in: tn =>
      -1L.assertPositive[IO]("Value is not positive").assertThrows[AssertionError]

  "assertEqual" should:
    "complete successfully when both values are equal" in: _ =>
      42.assertEqual[IO, Int](42, "Values must be equal").assertNoException

    "raise an error when values are not equal" in: _ =>
      42.assertEqual[IO, Int](43, "Values must be equal").assertThrows[AssertionError]

  "assertDistinct" should:
    "return the same sequence if all elements are distinct" in: _ =>
      val distinctSeq = List(1, 2, 3, 4, 5)
      List(1, 2, 3, 4, 5).assertDistinct[IO]("Elements are not distinct").assertNoException

    "raise an AssertionError if the sequence contains duplicates" in: _ =>
      List(1, 2, 2, 3, 4).assertDistinct[IO](
        "Elements are not distinct"
      ).assertThrowsWithMessage[AssertionError]("Elements are not distinct, seq: 1,2,2,3,4, duplicates: 2")

  "assertUniform" should:
    "return the sequence when all elements are the same" in: _ =>
      val seq = List(1, 1, 1)
      seq.assertUniform[IO]("Elements are not uniform").assertNoException

    "return the sequence when it is empty" in: _ =>
      val seq = List.empty[Int]
      seq.assertUniform[IO]("Elements are not uniform").assertNoException

    "raise an error when elements are not the same" in: _ =>
      val seq = List(1, 2, 1)
      seq.assertUniform[IO]("Elements are not uniform").assertThrows[AssertionError]

  "assertNonEmpty" should:
    "return the sequence when it is not empty" in: _ =>
      val seq = List(1, 2, 3)
      seq.assertNonEmpty[IO]("Sequence is empty").assertNoException

    "raise an error when the sequence is empty" in: _ =>
      List.empty[Int].assertNonEmpty[IO]("Sequence is empty").assertThrows[AssertionError]

  "assertEmpty" should:
    "return the sequence when it is empty" in: _ =>
      val seq = List.empty[Int]
      seq.assertEmpty[IO]("Sequence is empty").assertNoException

    "raise an error when the sequence is non empty" in: _ =>
      List(1).assertEmpty[IO]("Sequence is non empty").assertThrows[AssertionError]

  "assertNotContain" should:
    "return the collection when it does not contain the specified element" in: _ =>
      val collection = Set(1, 2, 3)
      val element = 4
      collection.assertNotContain[IO](element, "Collection contains the element").assertNoException

    "raise an error when the collection contains the specified element" in: _ =>
      val collection = Set(1, 2, 3)
      val element = 2
      collection
        .assertNotContain[IO](element, "Collection contains the element")
        .assertThrowsWithMessage[AssertionError]("Collection contains the element, seq: 1,2,3 contains: 2")

  "assertSameSize" should:
    "return the input tuple when both collections have the same size" in: _ =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b", "c")
      left.assertSameSize[IO, String](right, "Collections must have the same size").assertNoException

    "raise an error when collections have different sizes" in: _ =>
      val left = List(1, 2, 3)
      val right = Vector("a", "b")
      left.assertSameSize[IO, String](right, "Collections must have the same size").assertThrows[AssertionError]

  "assertSameElems" should:
    "return the tuple when both collections have the same elements regardless of order" in: _ =>
      val left = List(1, 2, 3)
      val right = List(3, 2, 1)
      left.assertSameElems[IO](right, "Collections do not have the same elements").assertNoException

    "raise an error when collections have different elements" in: _ =>
      val left = List(1, 2, 3)
      val right = List(4, 5, 6)
      left.assertSameElems[IO](right, "Collections do not have the same elements").assertThrows[AssertionError]

  "assertContainsAllOf" should:
    "return the collections when all elements in the second collection are present in the first" in: _ =>
      val left = Seq(1, 2, 3, 4)
      val right = Seq(2, 3)
      left.assertContainsAllOf[IO](right, "Not all elements are contained").assertNoException

    "raise an error when the second collection has elements not present in the first" in: _ =>
      val left = Seq(1, 2, 3)
      val right = Seq(4, 5)
      left.assertContainsAllOf[IO](right, "Not all elements are contained").assertThrows[AssertionError]

    "return the collections when the second collection is empty" in: _ =>
      val left = Seq(1, 2, 3)
      val right = Seq.empty[Int]
      left.assertContainsAllOf[IO](right, "Not all elements are contained").assertNoException

    "raise an error when the first collection is empty and the second is not" in: _ =>
      val left = Seq.empty[Int]
      val right = Seq(1, 2)
      left.assertContainsAllOf[IO](right, "Not all elements are contained").assertThrows[AssertionError]

  "assertContainsNoneOf" should:
    "return the input collections when no common elements exist" in: _ =>
      val left = List(1, 2, 3)
      val right = List(4, 5, 6)
      left.assertContainsNoneOf[IO](right, "No common elements").assertNoException

    "raise an error when common elements exist" in: _ =>
      val left = List(1, 2, 3)
      val right = List(3, 4, 5)
      left.assertContainsNoneOf[IO](right, "Common elements found").assertThrows[AssertionError]

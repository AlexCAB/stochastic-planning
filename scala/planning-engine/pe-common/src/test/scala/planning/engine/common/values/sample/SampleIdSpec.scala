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
| created: 2025-07-05 |||||||||||*/

package planning.engine.common.values.sample

import cats.effect.IO
import planning.engine.common.UnitSpecIO

class SampleIdSpec extends UnitSpecIO:
  "SampleId.toPropName" should:
    "return the correct property name for a valid SampleId" in: _ =>
      SampleId(123L).toPropName mustEqual "s123"

    "return the correct property name for the initial SampleId" in: _ =>
      SampleId.init.toPropName mustEqual "s1"

  "SampleId.fromPropName" should:
    "return a valid SampleId when the string starts with 's' followed by digits" in: _ =>
      SampleId.fromPropName[IO]("s123").asserting(_ mustEqual SampleId(123L))

    "raise an error when the string does not start with 's'" in: _ =>
      SampleId.fromPropName[IO]("123").assertThrows[AssertionError]

    "raise an error when the string contains non-digit characters after 's'" in: _ =>
      SampleId.fromPropName[IO]("s12a3").assertThrows[AssertionError]

    "raise an error when the string is too short to contain a valid ID" in: _ =>
      SampleId.fromPropName[IO]("s").assertThrows[AssertionError]

    "raise an error when the string is empty" in: _ =>
      SampleId.fromPropName[IO]("").assertThrows[AssertionError]

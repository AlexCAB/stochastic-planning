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

import cats.ApplicativeThrow
import cats.effect.IO
import planning.engine.common.UnitSpecIO

class ErrorsSpec extends UnitSpecIO:

  "assertionError" should:
    "raise an AssertionError with the given message" in:
      val errorMessage = "This is an assertion error"

      errorMessage.assertionError[IO, Unit]
        .assertThrowsWithMessage[AssertionError](errorMessage)

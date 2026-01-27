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
| created: 2026-01-26 |||||||||||*/

package planning.engine.common.values.edges

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import planning.engine.common.values.node.ConId

class EndIdsSpec extends AnyWordSpec with Matchers:

  "EndIds.swap" should:
    "swap source and target ids" in:
      val endIds = EndIds(ConId(1), ConId(2))
      val swapped = endIds.swap

      swapped.src mustBe ConId(2)
      swapped.trg mustBe ConId(1)

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
import planning.engine.common.values.node.HnId

class EndIdsSpec extends AnyWordSpec with Matchers:

  "EndIds.swap" should:
    "swap source and target ids" in:
      val endIds = EndIds(HnId(1), HnId(2))
      val swapped = endIds.swap

      swapped.src mustBe HnId(2)
      swapped.trg mustBe HnId(1)

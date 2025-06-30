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
| created: 2025-06-30 |||||||||||*/

package planning.engine.common.enums

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import planning.engine.common.values.db.Neo4j

class EdgeTypeSpec extends AnyWordSpec with Matchers:

  "EdgeType.toLabel" should:
    "return the correct label for EdgeType.THEN" in:
      EdgeType.THEN.toLabel mustEqual Neo4j.THEN_LABEL

    "return the correct label for EdgeType.LINK" in:
      EdgeType.LINK.toLabel mustEqual Neo4j.LINK_LABEL

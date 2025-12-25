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

import org.scalatest.EitherValues
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.must.Matchers
import planning.engine.common.values.db.Neo4j

class EdgeTypeSpec extends AnyWordSpecLike with Matchers with EitherValues:

  "EdgeType.toLabel" should:
    "return the correct label for EdgeType.THEN" in:
      EdgeType.THEN.toLabel mustEqual Neo4j.THEN_LABEL

    "return the correct label for EdgeType.LINK" in:
      EdgeType.LINK.toLabel mustEqual Neo4j.LINK_LABEL

  "EdgeType.fromLabel" should:
    "return THEN when the label matches Neo4j.THEN_LABEL" in:
      EdgeType.fromLabel(Neo4j.THEN_LABEL).value mustEqual EdgeType.THEN

    "return LINK when the label matches Neo4j.LINK_LABEL" in:
      EdgeType.fromLabel(Neo4j.LINK_LABEL).value mustEqual EdgeType.LINK

    "raise an error when the label is unknown" in:
      EdgeType.fromLabel("UNKNOWN_LABEL").left.value mustEqual "Unknown EdgeType label: UNKNOWN_LABEL"

    "handle case insensitivity for THEN_LABEL" in:
      EdgeType.fromLabel(Neo4j.THEN_LABEL.toLowerCase).value mustEqual EdgeType.THEN

    "handle case insensitivity for LINK_LABEL" in:
      EdgeType.fromLabel(Neo4j.LINK_LABEL.toLowerCase).value mustEqual EdgeType.LINK

  "EdgeType.isThen" should:
    "return true for THEN link" in:
      EdgeType.THEN.isThen mustBe true

    "return false for LINK link" in:
      EdgeType.LINK.isThen mustBe false

  "EdgeType.isLink" should:
    "return true for LINK link" in:
      EdgeType.LINK.isLink mustBe true

    "return false for THEN link" in:
      EdgeType.THEN.isLink mustBe false

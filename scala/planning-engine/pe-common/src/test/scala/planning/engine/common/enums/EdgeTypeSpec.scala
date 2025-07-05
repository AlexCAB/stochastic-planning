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

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.db.Neo4j

class EdgeTypeSpec extends UnitSpecIO:

  "EdgeType.toLabel" should:
    "return the correct label for EdgeType.THEN" in: _ =>
      EdgeType.THEN.toLabel mustEqual Neo4j.THEN_LABEL

    "return the correct label for EdgeType.LINK" in: _ =>
      EdgeType.LINK.toLabel mustEqual Neo4j.LINK_LABEL

  "EdgeType.fromLabel" should:
    "return THEN when the label matches Neo4j.THEN_LABEL" in: _ =>
      EdgeType.fromLabel[IO](Neo4j.THEN_LABEL).asserting(_ mustEqual EdgeType.THEN)

    "return LINK when the label matches Neo4j.LINK_LABEL" in: _ =>
      EdgeType.fromLabel[IO](Neo4j.LINK_LABEL).asserting(_ mustEqual EdgeType.LINK)

    "raise an error when the label is unknown" in: _ =>
      EdgeType.fromLabel[IO]("UNKNOWN_LABEL").assertThrows[AssertionError]

    "handle case insensitivity for THEN_LABEL" in: _ =>
      EdgeType.fromLabel[IO](Neo4j.THEN_LABEL.toLowerCase).asserting(_ mustEqual EdgeType.THEN)

    "handle case insensitivity for LINK_LABEL" in: _ =>
      EdgeType.fromLabel[IO](Neo4j.LINK_LABEL.toLowerCase).asserting(_ mustEqual EdgeType.LINK)

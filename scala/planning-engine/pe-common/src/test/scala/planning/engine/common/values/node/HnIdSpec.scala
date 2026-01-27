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
| created: 2026-01-27 |||||||||||*/

package planning.engine.common.values.node

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.db.Neo4j.{CONCRETE_LABEL, ABSTRACT_LABEL}

class HnIdSpec extends UnitSpecIO:
  "HnId.initCon" should:
    "initialize ConId" in: _ =>
      HnId.initCon mustBe ConId(1L)

  "HnId.initAbs" should:
    "initialize AbsId" in: _ =>
      HnId.initAbs mustBe AbsId(1L)

  "HnId.apply(Long, Set[String])" should:
    "create ConId when CONCRETE_LABEL is present" in: _ =>
      val rawId = 5L
      HnId[IO](rawId, Set("SomeLabel", CONCRETE_LABEL)).asserting(_ mustEqual ConId(rawId))

    "create AbsId when ABSTRACT_LABEL is present" in: _ =>
      val rawId = 10L
      HnId[IO](rawId, Set(ABSTRACT_LABEL, "OtherLabel")).asserting(_ mustEqual AbsId(rawId))

    "raise an error when neither label is present" in: _ =>
      HnId[IO](15L, Set("SomeLabel", "OtherLabel")).assertThrows[AssertionError]

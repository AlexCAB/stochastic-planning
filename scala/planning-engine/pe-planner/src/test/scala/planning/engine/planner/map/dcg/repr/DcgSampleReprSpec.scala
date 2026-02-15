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
| created: 2026-02-02 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.edge.EdgeKey
import planning.engine.planner.map.test.data.MapSampleTestData
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.samples.DcgSample

class DcgSampleReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapSampleTestData:
    lazy val n11 = HnId(11)
    lazy val n12 = HnId(12)
    lazy val n13 = HnId(13)
    lazy val n14 = HnId(14)

    lazy val n21 = HnId(21)
    lazy val n22 = HnId(22)
    lazy val n23 = HnId(23)

    lazy val n31 = HnId(31)
    lazy val n32 = HnId(32)

    lazy val n41 = HnId(41)

    lazy val sampleData = makeSampleData()

    lazy val edges = Set(
      // LINK Level 1 to 2
      EdgeKey.Link(n11, n21),
      EdgeKey.Link(n11, n22),
      EdgeKey.Link(n12, n22),
      EdgeKey.Link(n13, n22),
      EdgeKey.Link(n13, n23),
      EdgeKey.Link(n14, n23),
      // LINK Level 2 to 3
      EdgeKey.Link(n21, n31),
      EdgeKey.Link(n21, n32),
      EdgeKey.Link(n22, n32),
      EdgeKey.Link(n23, n32),
      // LINK Level 3 to 4
      EdgeKey.Link(n31, n41),
      EdgeKey.Link(n32, n41),
      // THEN path 1
      EdgeKey.Then(n11, n12),
      EdgeKey.Then(n12, n13),
      EdgeKey.Then(n13, n14),
      // THEN path 2
      EdgeKey.Then(n21, n22),
      EdgeKey.Then(n22, n23),
      // THEN path 2
      EdgeKey.Then(n23, n22),
      EdgeKey.Then(n22, n21),
      // THEN path 3
      EdgeKey.Then(n11, n21),
      EdgeKey.Then(n21, n31),
      EdgeKey.Then(n31, n41),
      EdgeKey.Then(n41, n32),
      EdgeKey.Then(n32, n23),
      EdgeKey.Then(n23, n14),
      // THEN path/loop 3
      EdgeKey.Then(n31, n32),
      EdgeKey.Then(n32, n31),
      // THEN path/loop 4
      EdgeKey.Then(n41, n41)
    )

    lazy val dcgSample = DcgSample(sampleData, edges)

  "DcgSampleRepr.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.dcgSample.repr
        logInfo(tn, s"DcgSample.repr = $strRepr")

        strRepr mustBe "[123]"

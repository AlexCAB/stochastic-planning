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
| created: 2026-03-03 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import planning.engine.common.values.node.MnId
import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.common.values.sample.SampleId

trait DcgSampleReprTestData extends DcgSampleTestData:
  import MeKey.{Link, Then}
  import MnId.{Abs, Con}

  lazy val c11 = Con(11)
  lazy val c12 = Con(12)
  lazy val c13 = Con(13)
  lazy val c14 = Con(14)

  lazy val a21 = Abs(21)
  lazy val a22 = Abs(22)
  lazy val a23 = Abs(23)

  lazy val a31 = Abs(31)
  lazy val a32 = Abs(32)

  lazy val a41 = Abs(41)
  lazy val a42 = Abs(42)

  lazy val allMnIds: Set[MnId] = Set(c11, c12, c13, c14, a21, a22, a23, a31, a32, a41, a42)

  lazy val complexDcgSample: DcgSample[IO] = makeDcgSample(SampleId(1001), Some("complexDcgSample"))(
    // LINK Level 1 to 2
    Link(c11, a21),
    Link(c11, a22),
    Link(c12, a22),
    Link(c13, a22),
    Link(c13, a23),
    Link(c14, a23),
    // LINK Level 2 to 3
    Link(a21, a31),
    Link(a21, a32),
    Link(a22, a32),
    Link(a23, a32),
    // LINK Level 3 to 4
    Link(a31, a41),
    Link(a32, a41),
    // THEN path 1
    Then(c11, c12),
    Then(c12, c13),
    Then(c13, c14),
    // THEN path 2
    Then(a21, a22),
    Then(a22, a23),
    // THEN path 2
    Then(a23, a22),
    Then(a22, a21),
    // THEN path 3
    Then(c11, a21),
    Then(a21, a31),
    Then(a31, a41),
    Then(a41, a32),
    Then(a32, a23),
    Then(a23, c14),
    // THEN path/loop 3
    Then(a31, a32),
    Then(a32, a31),
    // THEN path/loop 4
    Then(a41, a41),
    // THEN path/loop 4
    Then(a42, a42)
  )

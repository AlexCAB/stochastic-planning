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
| created: 2026-10-09 |||||||||||*/

package planning.engine.planner.mpi.test.data

import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.model.data.samples.Sample

trait SampleTestData:
  self: UnitSpecWithData =>

  import MeKey.{Link, Then}
  import MnId.{Abs, Con}

  // Nodes IDs

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

  // Links and paths

  // LINK Level 1 to 2
  lazy val level1toL2: Set[MeKey] = Set(
    Link(c11, a21),
    Link(c11, a22),
    Link(c12, a22),
    Link(c13, a22),
    Link(c13, a23),
    Link(c14, a23),
  )

  // LINK Level 2 to 3
  lazy val level2toL3: Set[MeKey] = Set(
    Link(a21, a31),
    Link(a21, a32),
    Link(a22, a32),
    Link(a23, a32),
  )

  // LINK Level 3 to 4
  lazy val level3toL4: Set[MeKey] = Set(
    Link(a31, a41),
    Link(a32, a41),
  )

  // THEN path 1
  lazy val path1: Set[MeKey] = Set(
    Then(c11, c12),
    Then(c12, c13),
    Then(c13, c14),
  )

  // THEN path 2
  lazy val path21: Set[MeKey] = Set(
    Then(a21, a22),
    Then(a22, a23),
  )

  // THEN path 2
  lazy val path22: Set[MeKey] = Set(
    Then(a23, a22),
    Then(a22, a21),
  )

  // THEN path 3
  lazy val path3: Set[MeKey] = Set(
    Then(c11, a21),
    Then(a21, a31),
    Then(a31, a41),
    Then(a41, a32),
    Then(a32, a23),
    Then(a23, c14),
  )

  // THEN path/loop 3
  lazy val pathLoop3: Set[MeKey] = Set(
    Then(a31, a32),
    Then(a32, a31),
  )

  // THEN path/loop 4
  lazy val pathLoop41: Set[MeKey] = Set(
    Then(a41, a41),
  )

  // THEN path/loop 4
  lazy val pathLoop42: Set[MeKey] = Set(
    Then(a42, a42),
  )

  lazy val allKeys: Set[MeKey] = Set(
    level1toL2,
    level2toL3,
    level3toL4,
    path1,
    path21,
    path22,
    path3,
    pathLoop3,
    pathLoop41,
    pathLoop42,
  ).flatten

  // Samples

  trait ComplexSamples:
    lazy val allEdgesSample: Sample = Sample(
      id = SampleId(1001),
      props = Sample.Props(probabilityCount = 10, utility = 0.5),
      info = Some(Sample.Info(Name("complexSample"), None)),
      edges = allKeys.map(k => Sample.Edge(k, HnIndex(0), HnIndex(0))),
    )

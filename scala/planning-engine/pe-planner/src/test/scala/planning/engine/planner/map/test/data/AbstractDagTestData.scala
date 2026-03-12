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
| created: 2026-01-22 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.unsafe.IORuntime
import planning.engine.common.graph.edges.EdgeKey
import planning.engine.common.values.sample.SampleId
import EdgeKey.{Link, Then}

trait AbstractDagTestData extends DcgGraphTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val c_1p1_101 = makeConDcgNode(id = 101, name = Some("con_1p1"))
  lazy val c_1p2_102 = makeConDcgNode(id = 102, name = Some("con_1p2"))

  lazy val c_1c1_111 = makeConDcgNode(id = 111, name = Some("con_1c1"))
  lazy val c_1c2_112 = makeConDcgNode(id = 112, name = Some("con_1c2"))
  lazy val c_1c3_113 = makeConDcgNode(id = 113, name = Some("con_1c3"))
  lazy val c_1c4_114 = makeConDcgNode(id = 114, name = Some("con_1c4"))

  lazy val c_1n1_121 = makeConDcgNode(id = 121, name = Some("con_1n1"))
  lazy val c_1n2_122 = makeConDcgNode(id = 122, name = Some("con_1n2"))

  lazy val all234ConNodes = Set(c_1p1_101, c_1p2_102, c_1c1_111, c_1c2_112, c_1c3_113, c_1c4_114, c_1n1_121, c_1n2_122)

  lazy val a_2p1_201 = makeAbsDcgNode(id = 201, name = Some("abs_2p1"))
  lazy val a_2p2_202 = makeAbsDcgNode(id = 202, name = Some("abs_2p2"))

  lazy val a_2c1_211 = makeAbsDcgNode(id = 211, name = Some("abs_2c1"))
  lazy val a_2c2_212 = makeAbsDcgNode(id = 212, name = Some("abs_2c2"))
  lazy val a_2c3_213 = makeAbsDcgNode(id = 213, name = Some("abs_2c3"))

  lazy val a_2n1_221 = makeAbsDcgNode(id = 221, name = Some("abs_2n1"))

  lazy val a_3p1_301 = makeAbsDcgNode(id = 301, name = Some("abs_3p1"))

  lazy val a_3c1_311 = makeAbsDcgNode(id = 311, name = Some("abs_3c1"))
  lazy val a_3c2_312 = makeAbsDcgNode(id = 312, name = Some("abs_3c2"))

  lazy val a_3n1_321 = makeAbsDcgNode(id = 321, name = Some("abs_3n1"))

  lazy val all234AbsNodes =
    Set(a_2p1_201, a_2p2_202, a_2c1_211, a_2c2_212, a_2c3_213, a_2n1_221, a_3p1_301, a_3c1_311, a_3c2_312, a_3n1_321)

  lazy val e_1c1_2c1 = Link(src = c_1c1_111.id, trg = a_2c1_211.id)
  lazy val e_1c2_2c2 = Link(src = c_1c2_112.id, trg = a_2c2_212.id)
  lazy val e_1c3_2c2 = Link(src = c_1c3_113.id, trg = a_2c2_212.id)
  lazy val e_1c4_2c3 = Link(src = c_1c4_114.id, trg = a_2c3_213.id)

  lazy val link_1to2 = List(
    e_1c1_2c1,
    e_1c2_2c2,
    e_1c3_2c2,
    e_1c4_2c3
  )

  lazy val e_2c1_3c1 = Link(src = a_2c1_211.id, trg = a_3c1_311.id)
  lazy val e_2c2_3c1 = Link(src = a_2c2_212.id, trg = a_3c1_311.id)
  lazy val e_2c2_3c2 = Link(src = a_2c2_212.id, trg = a_3c2_312.id)
  lazy val e_2c3_3c2 = Link(src = a_2c3_213.id, trg = a_3c2_312.id)

  lazy val link_2to3 = List(
    e_2c1_3c1,
    e_2c2_3c1,
    e_2c2_3c2,
    e_2c3_3c2
  )

  lazy val e_1p1_1c1 = Then(src = c_1p1_101.id, trg = c_1c1_111.id)
  lazy val e_1p1_1c3 = Then(src = c_1p1_101.id, trg = c_1c3_113.id)
  lazy val e_1p2_1c2 = Then(src = c_1p2_102.id, trg = c_1c2_112.id)
  lazy val e_1p2_1c4 = Then(src = c_1p2_102.id, trg = c_1c4_114.id)

  lazy val then_prev1 = List(
    e_1p1_1c1,
    e_1p1_1c3,
    e_1p2_1c2,
    e_1p2_1c4
  )

  lazy val e_1c1_1c2 = Then(src = c_1c1_111.id, trg = c_1c2_112.id)
  lazy val e_1c2_1c3 = Then(src = c_1c2_112.id, trg = c_1c3_113.id)
  lazy val e_1c3_1c4 = Then(src = c_1c3_113.id, trg = c_1c4_114.id)

  lazy val thenE1 = List(
    e_1c1_1c2,
    e_1c2_1c3,
    e_1c3_1c4
  )

  lazy val e_1c1_1n1 = Then(src = c_1c1_111.id, trg = c_1n1_121.id)
  lazy val e_1c4_1n2 = Then(src = c_1c4_114.id, trg = c_1n2_122.id)

  lazy val then_next1 = List(
    e_1c1_1n1,
    e_1c4_1n2
  )

  lazy val e_2p1_2c1 = Then(src = a_2p1_201.id, trg = a_2c1_211.id)
  lazy val e_2p2_2c3 = Then(src = a_2p2_202.id, trg = a_2c3_213.id)

  lazy val then_prev2 = List(
    e_2p1_2c1,
    e_2p2_2c3
  )

  lazy val e_2c1_2c2 = Then(src = a_2c1_211.id, trg = a_2c2_212.id)
  lazy val e_2c2_2c3 = Then(src = a_2c2_212.id, trg = a_2c3_213.id)

  lazy val thenE2 = List(
    e_2c1_2c2,
    e_2c2_2c3
  )

  lazy val e_2c2_2n1 = Then(src = a_2c2_212.id, trg = a_2n1_221.id)

  lazy val then_next2 = List(
    e_2c2_2n1
  )

  lazy val e_3p1_3c1 = Then(src = a_3p1_301.id, trg = a_3c1_311.id)
  lazy val e_3p1_3c2 = Then(src = a_3p1_301.id, trg = a_3c2_312.id)

  lazy val then_prev3 = List(
    e_3p1_3c1,
    e_3p1_3c2
  )

  lazy val e_3c1_3c2 = Then(src = a_3c1_311.id, trg = a_3c2_312.id)

  lazy val thenE3 = List(
    e_3c1_3c2
  )

  lazy val e_3c1_3n1 = Then(src = a_3c1_311.id, trg = a_3n1_321.id)
  lazy val e_3c2_3n1 = Then(src = a_3c2_312.id, trg = a_3n1_321.id)

  lazy val then_next3 = List(
    e_3c1_3n1,
    e_3c2_3n1
  )

  lazy val allDcg234Edges: List[EdgeKey] = List(
    link_1to2,
    link_2to3,
    then_prev1,
    thenE1,
    then_next1,
    then_prev2,
    thenE2,
    then_next2,
    then_prev3,
    thenE3,
    then_next3
  ).flatten

  // Main structure:
  //
  //  C<1c1.111> -> C<1c2.112> -> C<1c3.113> -> C<1c4.114>
  //      \                  \         |              |
  //       \                  \       |              |
  //        v                  v     v              v
  //      A(2c1.211)    ->   A(2c2.212)   ->    A(2c3.213)
  //               \            |      \           |
  //                \          |        \         |
  //                 v        v          v       v
  //                 A(3c1.311)   ->   A(3c2.312)
  //
  // Prev edges (level 0, then_prev1):
  //   C(1p1.101) -> C(1c1.111)
  //   C(1p1.101) -> C(1c3.113)
  //   C(1p2.102) -> C(1c2.112)
  //   C(1p2.102) -> C(1c4.114)
  //
  // Next edges (level 0, then_next1):
  //   C(1c1.111) -> C(1n1.121)
  //   C(1c4.114) -> C(1n2.122)
  //
  // Prev edges (level 1, then_prev2):
  //   A(2p1.201) -> A(2c1.211)
  //   A(2p2.202) -> A(2c3.213)
  //
  // Next edges (level 1, then_next2):
  //   A(2c2.212) -> A(2n1.221)
  //
  // Prev edges (level 2, then_prev3):
  //   A(3p1.301) -> A(3c1.311)
  //   A(3p1.301) -> A(3c2.312)
  //
  // Next edges (level 2, then_next3):
  //   A(3c1.311) -> A(3n1.321)
  //   A(3c2.312) -> A(3n1.321)
  lazy val dcg234SampleAllEdges = makeDcgSampleAdd(SampleId(1001), Some("dcg234SampleAllEdges"))(allDcg234Edges*)

  lazy val dcg234SampleLine = makeDcgSampleAdd(SampleId(1002), Some("dcg234SampleLine"))(
    // Abstraction edges:
    e_1c1_2c1,
    e_2c1_3c1,
    // Previous edges:
    e_1p1_1c1,
    e_2p1_2c1,
    e_3p1_3c1,
    // Next edges:
    e_1c1_1n1,
    e_3c1_3n1
  )

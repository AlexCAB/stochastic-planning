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

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.node.MnId.{Con, Abs}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.edge.EdgeKey.{Link, Then}

trait SimpleAbstractForestTestData extends DcgGraphTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val con_1p1 = makeConDcgNode(id = Con(101))
  lazy val con_1p2 = makeConDcgNode(id = Con(102))

  lazy val con_1c1 = makeConDcgNode(id = Con(111))
  lazy val con_1c2 = makeConDcgNode(id = Con(112))
  lazy val con_1c3 = makeConDcgNode(id = Con(113))
  lazy val con_1c4 = makeConDcgNode(id = Con(114))

  lazy val con_1n1 = makeConDcgNode(id = Con(121))
  lazy val con_1n2 = makeConDcgNode(id = Con(122))

  lazy val all234ConNodes = List(con_1p1, con_1p2, con_1c1, con_1c2, con_1c3, con_1c4, con_1n1, con_1n2)

  lazy val abs_2p1 = makeAbsDcgNode(id = Abs(201))
  lazy val abs_2p2 = makeAbsDcgNode(id = Abs(202))

  lazy val abs_2c1 = makeAbsDcgNode(id = Abs(211))
  lazy val abs_2c2 = makeAbsDcgNode(id = Abs(212))
  lazy val abs_2c3 = makeAbsDcgNode(id = Abs(213))

  lazy val abs_2n1 = makeAbsDcgNode(id = Abs(221))

  lazy val abs_3p1 = makeAbsDcgNode(id = Abs(301))

  lazy val abs_3c1 = makeAbsDcgNode(id = Abs(311))
  lazy val abs_3c2 = makeAbsDcgNode(id = Abs(312))

  lazy val abs_3n1 = makeAbsDcgNode(id = Abs(321))

  lazy val all234AbsNodes =
    List(abs_2p1, abs_2p2, abs_2c1, abs_2c2, abs_2c3, abs_2n1, abs_3p1, abs_3c1, abs_3c2, abs_3n1)

  lazy val e_1c1_2c1 = Link(src = con_1c1.id, trg = abs_2c1.id)
  lazy val e_1c2_2c2 = Link(src = con_1c2.id, trg = abs_2c2.id)
  lazy val e_1c3_2c2 = Link(src = con_1c3.id, trg = abs_2c2.id)
  lazy val e_1c4_2c3 = Link(src = con_1c4.id, trg = abs_2c3.id)

  lazy val link_1to2 = List(
    e_1c1_2c1,
    e_1c2_2c2,
    e_1c3_2c2,
    e_1c4_2c3
  )

  lazy val e_2c1_3c1 = Link(src = abs_2c1.id, trg = abs_3c1.id)
  lazy val e_2c2_3c1 = Link(src = abs_2c2.id, trg = abs_3c1.id)
  lazy val e_2c2_3c2 = Link(src = abs_2c2.id, trg = abs_3c2.id)
  lazy val e_2c3_3c2 = Link(src = abs_2c3.id, trg = abs_3c2.id)

  lazy val link_2to3 = List(
    e_2c1_3c1,
    e_2c2_3c1,
    e_2c2_3c2,
    e_2c3_3c2
  )

  lazy val e_1p1_1c1 = Then(src = con_1p1.id, trg = con_1c1.id)
  lazy val e_1p1_1c3 = Then(src = con_1p1.id, trg = con_1c3.id)
  lazy val e_1p2_1c2 = Then(src = con_1p2.id, trg = con_1c2.id)
  lazy val e_1p2_1c4 = Then(src = con_1p2.id, trg = con_1c4.id)

  lazy val then_prev1 = List(
    e_1p1_1c1,
    e_1p1_1c3,
    e_1p2_1c2,
    e_1p2_1c4
  )

  lazy val e_1c1_1c2 = Then(src = con_1c1.id, trg = con_1c2.id)
  lazy val e_1c2_1c3 = Then(src = con_1c2.id, trg = con_1c3.id)
  lazy val e_1c3_1c4 = Then(src = con_1c3.id, trg = con_1c4.id)

  lazy val thenE1 = List(
    e_1c1_1c2,
    e_1c2_1c3,
    e_1c3_1c4
  )

  lazy val e_1c1_1n1 = Then(src = con_1c1.id, trg = con_1n1.id)
  lazy val e_1c4_1n2 = Then(src = con_1c4.id, trg = con_1n2.id)

  lazy val then_next1 = List(
    e_1c1_1n1,
    e_1c4_1n2
  )

  lazy val e_2p1_2c1 = Then(src = abs_2p1.id, trg = abs_2c1.id)
  lazy val e_2p2_2c3 = Then(src = abs_2p2.id, trg = abs_2c3.id)

  lazy val then_prev2 = List(
    e_2p1_2c1,
    e_2p2_2c3
  )

  lazy val e_2c1_2c2 = Then(src = abs_2c1.id, trg = abs_2c2.id)
  lazy val e_2c2_2c3 = Then(src = abs_2c2.id, trg = abs_2c3.id)

  lazy val thenE2 = List(
    e_2c1_2c2,
    e_2c2_2c3
  )

  lazy val e_2c2_2n1 = Then(src = abs_2c2.id, trg = abs_2n1.id)

  lazy val then_next2 = List(
    e_2c2_2n1
  )

  lazy val e_3p1_3c1 = Then(src = abs_3p1.id, trg = abs_3c1.id)
  lazy val e_3p1_3c2 = Then(src = abs_3p1.id, trg = abs_3c2.id)

  lazy val then_prev3 = List(
    e_3p1_3c1,
    e_3p1_3c2
  )

  lazy val e_3c1_3c2 = Then(src = abs_3c1.id, trg = abs_3c2.id)

  lazy val thenE3 = List(
    e_3c1_3c2
  )

  lazy val e_3c1_3n1 = Then(src = abs_3c1.id, trg = abs_3n1.id)
  lazy val e_3c2_3n1 = Then(src = abs_3c2.id, trg = abs_3n1.id)

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

  lazy val dcgGraph234Empty = DcgGraph(all234ConNodes ++ all234AbsNodes, List(), List()).unsafeRunSync()

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
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.planner.map.dcg.nodes.DcgNode

trait SimpleAbstractForestTestData extends SimpleMemStateTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val conNl1p1: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(101))
  lazy val conNl1p2: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(102))

  lazy val conNl1c1: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(111))
  lazy val conNl1c2: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(112))
  lazy val conNl1c3: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(113))
  lazy val conNl1c4: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(114))

  lazy val conNl1n1: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(121))
  lazy val conNl1n2: DcgNode.Concrete[IO] = makeConcreteDcgNode(id = HnId(122))

  lazy val all234ConNodes: List[DcgNode.Concrete[IO]] =
    List(conNl1p1, conNl1p2, conNl1c1, conNl1c2, conNl1c3, conNl1c4, conNl1n1, conNl1n2)

  lazy val absNl2p1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(201))
  lazy val absNl2p2: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(202))

  lazy val absNl2c1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(211))
  lazy val absNl2c2: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(212))
  lazy val absNl2c3: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(213))

  lazy val absNl2n1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(221))

  lazy val absNl3p1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(301))

  lazy val absNl3c1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(311))
  lazy val absNl3c2: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(312))

  lazy val absNl3n1: DcgNode.Abstract[IO] = makeAbstractDcgNode(id = HnId(321))

  lazy val all234AbsNodes: List[DcgNode.Abstract[IO]] =
    List(absNl2p1, absNl2p2, absNl2c1, absNl2c2, absNl2c3, absNl2n1, absNl3p1, absNl3c1, absNl3c2, absNl3n1)

  lazy val linkE1to2: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = conNl1c1.id, trgId = absNl2c1.id),
    makeDcgEdgeData(srcId = conNl1c2.id, trgId = absNl2c2.id),
    makeDcgEdgeData(srcId = conNl1c3.id, trgId = absNl2c2.id),
    makeDcgEdgeData(srcId = conNl1c4.id, trgId = absNl2c3.id)
  )

  lazy val linkE2to3: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl2c1.id, trgId = absNl3c1.id),
    makeDcgEdgeData(srcId = absNl2c2.id, trgId = absNl3c1.id),
    makeDcgEdgeData(srcId = absNl2c2.id, trgId = absNl3c2.id),
    makeDcgEdgeData(srcId = absNl2c3.id, trgId = absNl3c2.id)
  )

  lazy val thenEPrev1: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = conNl1p1.id, trgId = conNl1c1.id),
    makeDcgEdgeData(srcId = conNl1p1.id, trgId = conNl1c3.id),
    makeDcgEdgeData(srcId = conNl1p2.id, trgId = conNl1c2.id),
    makeDcgEdgeData(srcId = conNl1p2.id, trgId = conNl1c4.id)
  )

  lazy val thenE1: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = conNl1c1.id, trgId = conNl1c2.id),
    makeDcgEdgeData(srcId = conNl1c2.id, trgId = conNl1c3.id),
    makeDcgEdgeData(srcId = conNl1c3.id, trgId = conNl1c4.id)
  )

  lazy val thenENext1: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = conNl1c1.id, trgId = conNl1n1.id),
    makeDcgEdgeData(srcId = conNl1c1.id, trgId = conNl1n1.id),
    makeDcgEdgeData(srcId = conNl1c4.id, trgId = conNl1n2.id)
  )

  lazy val thenEPrev2: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl2p1.id, trgId = absNl2c1.id),
    makeDcgEdgeData(srcId = absNl2p2.id, trgId = absNl2c3.id)
  )

  lazy val thenE2: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl2c1.id, trgId = absNl2c2.id),
    makeDcgEdgeData(srcId = absNl2c2.id, trgId = absNl2c3.id)
  )

  lazy val thenENext2: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl2c2.id, trgId = absNl2n1.id)
  )

  lazy val thenEPrev3: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl3p1.id, trgId = absNl3c1.id),
    makeDcgEdgeData(srcId = absNl3p1.id, trgId = absNl3c2.id)
  )

  lazy val thenE3: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl3c1.id, trgId = absNl3c2.id)
  )

  lazy val thenENext3: List[DcgEdgeData] = List(
    makeDcgEdgeData(srcId = absNl3c1.id, trgId = absNl3n1.id),
    makeDcgEdgeData(srcId = absNl3c2.id, trgId = absNl3n1.id)
  )

  lazy val dcg234EdgesLinks: List[DcgEdgeData] = linkE1to2 ++ linkE2to3
  lazy val dcg234Edges1: List[DcgEdgeData] = thenEPrev1 ++ thenE1 ++ thenENext1
  lazy val dcg234Edges2: List[DcgEdgeData] = thenEPrev2 ++ thenE2 ++ thenENext2
  lazy val dcg234Edges3: List[DcgEdgeData] = thenEPrev3 ++ thenE3 ++ thenENext3

  lazy val allDcg234Edges: List[DcgEdgeData] = dcg234EdgesLinks ++ dcg234Edges1 ++ dcg234Edges2 ++ dcg234Edges3

  // Main structure:
  //
  //  C(l1n1.111) -> C(l1n2.112) -> C(l1n3.113) -> C(l1n4.114)
  //      \                  \         |                  |
  //       \                  \       |                  |
  //        v                  v     v                  v
  //      A(l2n1.211)   ->   A(l2n2.212)   ->   A(l2n3.213)
  //               \            |      \           |
  //                \          |        \         |
  //                 v        v          v       v
  //                 A(l3n1.311)   ->   A(l3n2.312)
  //
  // Prev edges (level 1, thenEPrev1):
  //   C(l1p1.101) -> C(l1n1.111)
  //   C(l1p1.101) -> C(l1n3.113)
  //   C(l1p2.102) -> C(l1n2.112)
  //   C(l1p2.102) -> C(l1n4.114)
  //
  // Prev edges (level 2, thenEPrev2):
  //   A(l2p1.201) -> A(l2n1.211)
  //   A(l2p2.202) -> A(l2n3.213)
  //
  // Prev edges (level 3, thenEPrev3):
  //   A(l3p1.301) -> A(l3n1.311)
  //   A(l3p1.301) -> A(l3n2.312)
  lazy val dcgGraph234Empty = DcgGraph.empty[IO]
    .addConNodes(all234ConNodes)
    .flatMap(_.addAbsNodes(all234AbsNodes))
    .flatMap(_.addEdges(allDcg234Edges))
    .unsafeRunSync()

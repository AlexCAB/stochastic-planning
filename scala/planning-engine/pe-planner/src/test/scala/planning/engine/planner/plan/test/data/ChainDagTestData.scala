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
| created: 2026-03-20 |||||||||||*/

package planning.engine.planner.plan.test.data

import cats.effect.IO
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.PnId
import planning.engine.planner.plan.dag.nodes.DagNode
import planning.engine.planner.plan.test.data.DaGraphTestData

trait ChainDagTestData extends DaGraphTestData:

  lazy val c_1_1 = makeConPnId(mnId1, count = 1)
  lazy val c_1_2 = makeConPnId(mnId1, count = 2)
  lazy val c_2_2 = makeConPnId(mnId2, count = 1)
  lazy val c_1_3 = makeConPnId(mnId1, count = 3)

  lazy val a1_1_1 = makeAbsPnId(mnId3, count = 1)
  lazy val a1_2_1 = makeAbsPnId(mnId4, count = 1)
  lazy val a1_1_2 = makeAbsPnId(mnId3, count = 2)
  lazy val a1_1_3 = makeAbsPnId(mnId3, count = 3)
  lazy val a1_2_3 = makeAbsPnId(mnId4, count = 2)

  lazy val a2_1_1 = makeAbsPnId(mnId5, count = 1)
  lazy val a2_1_2 = makeAbsPnId(mnId5, count = 2)
  lazy val a2_2_2 = makeAbsPnId(mnId6, count = 1)
  lazy val a2_1_3 = makeAbsPnId(mnId5, count = 3)

  lazy val conPnIds: Map[PnId.Con, (String, Option[IoTime])] = Map(
    c_1_1 -> ("c_1_1", Some(IoTime(1L))),
    c_1_2 -> ("c_1_2", Some(IoTime(2L))),
    c_2_2 -> ("c_2_2", Some(IoTime(2L))),
    c_1_3 -> ("c_1_3", None)
  )

  lazy val absPnIds: Map[PnId.Abs, (String, Option[IoTime])] = Map(
    a1_1_1 -> ("a1_1_1", Some(IoTime(1L))),
    a1_2_1 -> ("a1_2_1", Some(IoTime(1L))),
    a1_1_2 -> ("a1_1_2", Some(IoTime(2L))),
    a1_1_3 -> ("a1_1_3", None),
    a1_2_3 -> ("a1_2_3", None),
    a2_1_1 -> ("a2_1_1", Some(IoTime(1L))),
    a2_1_2 -> ("a2_1_2", Some(IoTime(2L))),
    a2_2_2 -> ("a2_2_2", Some(IoTime(2L))),
    a2_1_3 -> ("a2_1_3", None)
  )

  lazy val dagNodes: Set[DagNode[IO]] = (conPnIds ++ absPnIds)
    .map((id, params) => makeDagNode(id = id, time = params._2, name = Some(params._1)))
    .toSet

  lazy val linkEdges = List(
    makeDagEdgeLink(c_1_1, a1_1_1),
    makeDagEdgeLink(c_1_1, a1_2_1),
    makeDagEdgeLink(a1_1_1, a2_1_1),
    makeDagEdgeLink(a1_2_1, a2_1_1),
    makeDagEdgeLink(c_1_1, a1_1_2),
    makeDagEdgeLink(c_1_2, a1_1_2),
    makeDagEdgeLink(c_2_2, a1_1_2),
    makeDagEdgeLink(a1_1_2, a2_1_2),
    makeDagEdgeLink(a1_1_2, a2_2_2),
    makeDagEdgeLink(a1_1_2, a2_1_3),
    makeDagEdgeLink(c_1_3, a1_1_3),
    makeDagEdgeLink(c_1_3, a1_2_3),
    makeDagEdgeLink(a1_1_3, a2_1_3),
    makeDagEdgeLink(a1_2_3, a2_1_3),
  )

  lazy val thenEdges = List(
    makeDagEdgeThen(c_1_1, c_1_2),
    makeDagEdgeThen(c_1_2, c_1_3),
    makeDagEdgeThen(c_1_1, c_2_2),
    makeDagEdgeThen(c_1_1, a1_1_1),
    makeDagEdgeThen(a1_1_1, a1_1_2),
    makeDagEdgeThen(a1_1_2, a1_1_3),
    makeDagEdgeThen(a1_2_1, a2_1_1),
    makeDagEdgeThen(a1_2_1, a2_1_1),
    makeDagEdgeThen(a2_1_1, a2_1_2),
    makeDagEdgeThen(a2_1_2, a2_1_3),
    makeDagEdgeThen(a2_1_1, a2_2_2),
  )

  // The structure of chainDaGraph is:
  //
  //                  Time 1                     Time 2                     Time ?
  // CONCRETE
  //                  [c_1_1] ──────then──────▶ [c_1_2] ──────then──────▶ [c_1_3]
  //                     └───────────then──────▶ [c_2_2]                     │
  //                     │                          │                        │
  //                     │link,then                 │link                    │link
  //                     ▼                          ▼                        ▼
  // ABSTRACT-1
  //                 (a1_1_1) ─────then─────▶ (a1_1_2) ──────then──────▶ (a1_1_3)
  //                 (a1_2_1)                                            (a1_2_3)
  //                     │                          │                        │
  //                     │link,then                 │link                    │link
  //                     ▼                          ▼                        ▼
  // ABSTRACT-2
  //                 (a2_1_1) ─────then─────▶ (a2_1_2) ──────then──────▶ (a2_1_3)
  //                    └───────────then─────▶ (a2_2_2)
  //
  //  Additional link edges not shown above:
  //    c_1_1 ──link──▶ a1_2_1, a1_1_2    c_2_2 ──link──▶ a1_1_2    c_1_3 ──link──▶ a1_2_3
  //    a1_2_1 ──link──▶ a2_1_1    a1_1_2 ──link──▶ a2_2_2, a2_1_3    a1_2_3 ──link──▶ a2_1_3
  //
  lazy val chainDaGraph = makeDaGraph(dagNodes, linkEdges ++ thenEdges)

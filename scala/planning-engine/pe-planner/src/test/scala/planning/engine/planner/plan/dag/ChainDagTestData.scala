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

package planning.engine.planner.plan.dag

import cats.effect.IO
import planning.engine.common.values.node.PnId
import planning.engine.planner.plan.test.data.DaGraphTestData
import planning.engine.planner.plan.dag.nodes.DagNode

trait ChainDagTestData extends DaGraphTestData:

  lazy val c_1_1 = makeConPnId(mnId1, time = 1)
  lazy val c_1_2 = makeConPnId(mnId1, time = 2)
  lazy val c_2_2 = makeConPnId(mnId2, time = 2)
  lazy val c_1_3 = makeConPnId(mnId1, time = 3)

  lazy val a1_1_1 = makeAbsPnId(mnId3, time = 1)
  lazy val a1_2_1 = makeAbsPnId(mnId4, time = 1)
  lazy val a1_1_2 = makeAbsPnId(mnId3, time = 2)
  lazy val a1_1_3 = makeAbsPnId(mnId3, time = 3)
  lazy val a1_2_3 = makeAbsPnId(mnId4, time = 3)

  lazy val a2_1_1 = makeAbsPnId(mnId5, time = 1)
  lazy val a2_1_2 = makeAbsPnId(mnId5, time = 2)
  lazy val a2_2_2 = makeAbsPnId(mnId6, time = 2)
  lazy val a2_1_3 = makeAbsPnId(mnId5, time = 3)

  lazy val conPnIds: Map[PnId.Con, String] = Map(
    c_1_1 -> "c_1_1",
    c_1_2 -> "c_1_2",
    c_2_2 -> "c_2_2",
    c_1_3 -> "c_1_3",
  )

  lazy val absPnIds: Map[PnId.Abs, String] = Map(
    a1_1_1 -> "a1_1_1",
    a1_2_1 -> "a1_2_1",
    a1_1_2 -> "a1_1_2",
    a1_1_3 -> "a1_1_3",
    a1_2_3 -> "a1_2_3",
    a2_1_1 -> "a2_1_1",
    a2_1_2 -> "a2_1_2",
    a2_2_2 -> "a2_2_2",
    a2_1_3 -> "a2_1_3",
  )

  lazy val dagNodes: Set[DagNode[IO]] = (conPnIds ++ absPnIds)
    .map((id, name) => makeDagNode(id = id, name = Some(name)))
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
    makeDagEdgeLink(a1_2_3, a2_1_3)
  )

  lazy val thenEdges = List(
    makeDagEdgeThen(c_1_1, c_1_2),
    makeDagEdgeThen(c_1_1, c_2_2),
    makeDagEdgeThen(c_1_2, c_1_3),
    makeDagEdgeThen(c_2_2, c_1_3),
    makeDagEdgeThen(a1_1_1, a1_1_2),
    makeDagEdgeThen(a1_2_1, a1_1_2),
    makeDagEdgeThen(a1_1_2, a1_1_3),
    makeDagEdgeThen(a1_1_2, a1_2_3),
    makeDagEdgeThen(a2_1_1, a2_1_2),
    makeDagEdgeThen(a2_1_1, a2_2_2),
    makeDagEdgeThen(a2_1_2, a2_1_3),
    makeDagEdgeThen(a2_2_2, a2_1_3)
  )

  lazy val chainDaGraph = makeDaGraph(dagNodes, linkEdges ++ thenEdges)

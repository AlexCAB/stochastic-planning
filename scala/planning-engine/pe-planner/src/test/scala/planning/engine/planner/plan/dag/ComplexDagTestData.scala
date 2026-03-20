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

import planning.engine.planner.plan.test.data.DaGraphTestData

trait ComplexDagTestData extends DaGraphTestData:

  lazy val c_1_1 = makeConSnId(mnId1, time = 1)
  lazy val c_1_2 = makeConSnId(mnId1, time = 2)
  lazy val c_2_2 = makeConSnId(mnId2, time = 2)
  lazy val c_1_3 = makeConSnId(mnId1, time = 3)

  lazy val a1_1_1 = makeAbsSnId(mnId3, time = 1)
  lazy val a1_2_1 = makeAbsSnId(mnId4, time = 1)
  lazy val a1_1_2 = makeAbsSnId(mnId3, time = 2)
  lazy val a1_1_3 = makeAbsSnId(mnId3, time = 3)
  lazy val a1_2_3 = makeAbsSnId(mnId4, time = 3)

  lazy val a2_1_1 = makeAbsSnId(mnId5, time = 1)
  lazy val a2_1_2 = makeAbsSnId(mnId5, time = 2)
  lazy val a2_2_2 = makeAbsSnId(mnId6, time = 2)
  lazy val a2_1_3 = makeAbsSnId(mnId5, time = 3)

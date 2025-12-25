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
| created: 2025-12-22 |||||||||||*/

package planning.engine.planner.map.dcg.state

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId

class IdsCountStateSpec extends AnyWordSpecLike with Matchers:
  lazy val initState = IdsCountState.init

  lazy val hnId1 = HnId(1)
  lazy val hnId2 = HnId(2)
  lazy val hnId3 = HnId(3)
  lazy val hnId4 = HnId(4)
  lazy val hnId5 = HnId(5)

  "IdsCountState.isInit" should:
    "be true for initial state" in:
      initState.isInit mustBe true

    "be false for non-initial state" in:
      val (state1, _) = initState.getNextHdIds(1)
      state1.isInit mustBe false

      val (state2, _) = initState.getNextSampleIds(1)
      state2.isInit mustBe false

      val (state3, _) = initState.getNextHnIndexes(Set(hnId1))
      state3.isInit mustBe false

  "IdsCountState.getNextHdIds(...)" should:
    "get next hd ids and update state" in:
      val (state1, hdIds1) = initState.getNextHdIds(3)
      hdIds1 mustBe List(hnId1, hnId2, hnId3)
      state1.nextHdId mustBe 4L

      val (state2, hdIds2) = state1.getNextHdIds(2)
      hdIds2 mustBe List(hnId4, hnId5)
      state2.nextHdId mustBe 6L

  "IdsCountState.getNextSampleIds(...)" should:
    "get next sample ids and update state" in:
      val (state1, sampleIds1) = initState.getNextSampleIds(2)
      sampleIds1 mustBe List(SampleId(1), SampleId(2))
      state1.nextSampleId mustBe 3L

      val (state2, sampleIds2) = state1.getNextSampleIds(3)
      sampleIds2 mustBe List(SampleId(3), SampleId(4), SampleId(5))
      state2.nextSampleId mustBe 6L

  "IdsCountState.getNextHnIndexes(...)" should:
    "get next hn indexes for given hn ids and update state" in:
      val (state1, hnIndexes1) = initState.getNextHnIndexes(Set(hnId1, hnId2))
      hnIndexes1 mustBe Map(hnId1 -> HnIndex(1), hnId2 -> HnIndex(1))
      state1.nextHnIndexMap mustBe Map(hnId1 -> 2L, hnId2 -> 2L)

      val (state2, hnIndexes2) = state1.getNextHnIndexes(Set(hnId1, hnId2, hnId3))
      hnIndexes2 mustBe Map(hnId1 -> HnIndex(2), hnId2 -> HnIndex(2), hnId3 -> HnIndex(1))
      state2.nextHnIndexMap mustBe Map(hnId1 -> 3L, hnId2 -> 3L, hnId3 -> 2L)

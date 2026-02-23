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

package planning.engine.planner.map.state

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import planning.engine.common.values.node.{MnId, HnIndex}
import planning.engine.common.values.node.MnId.{Con, Abs}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.state.MapIdsCountState

class MapIdsCountStateSpec extends AnyWordSpecLike with Matchers:
  lazy val initState = MapIdsCountState.init

  "IdsCountState.nextNId(...)" should:
    "get next hd ids and update state" in:
      val (state1, hdIds1) = initState.nextNId(3, MnId.Con.apply)
      hdIds1 mustBe List(Con(1), Con(2), Con(3))
      state1.nextMnId mustBe 4L

      val (state2, hdIds2) = state1.nextNId(2, MnId.Abs.apply)
      hdIds2 mustBe List(Abs(4), Abs(5))
      state2.nextMnId mustBe 6L

  "IdsCountState.getNextConIds(...)" should:
    "get next concrete hd ids and update state" in:
      val (state1, conIds1) = initState.getNextConIds(2)
      conIds1 mustBe List(Con(1), Con(2))
      state1.nextMnId mustBe 3L

  "IdsCountState.getNextAbsIds(...)" should:
    "get next abstract hd ids and update state" in:
      val (state1, absIds1) = initState.getNextAbsIds(3)
      absIds1 mustBe List(Abs(1), Abs(2), Abs(3))
      state1.nextMnId mustBe 4L

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
      val mnId1 = Con(1)
      val mnId2 = Abs(2)
      val mnId3 = Con(3)

      val (state1, hnIndexes1) = initState.getNextHnIndexes(Set(mnId1, mnId2))
      hnIndexes1.indexies mustBe Map(mnId1 -> HnIndex(1), mnId2 -> HnIndex(1))
      state1.nextHnIndexMap mustBe Map(mnId1 -> 2L, mnId2 -> 2L)

      val (state2, hnIndexes2) = state1.getNextHnIndexes(Set(mnId1, mnId2, mnId3))
      hnIndexes2.indexies mustBe Map(mnId1 -> HnIndex(2), mnId2 -> HnIndex(2), mnId3 -> HnIndex(1))
      state2.nextHnIndexMap mustBe Map(mnId1 -> 3L, mnId2 -> 3L, mnId3 -> 2L)

  "IdsCountState.init" should:
    "be true for initial state" in:
      initState.nextMnId mustBe 1L
      initState.nextSampleId mustBe 1L
      initState.nextHnIndexMap mustBe Map.empty

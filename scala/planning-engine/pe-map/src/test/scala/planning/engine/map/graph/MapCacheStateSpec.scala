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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.graph

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import planning.engine.common.properties.*

import scala.collection.immutable.Queue
import cats.effect.cps.*
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.node.HiddenNode

class MapCacheStateSpec extends UnitSpecIO with MapGraphTestData:

  private class CaseData extends Case:
    lazy val testEmptyState = MapCacheState[IO](
      hiddenNodes = Map.empty,
      samples = Map.empty,
      sampleCount = 10L,
      hnQueue = Queue.empty
    )

    lazy val testState10Nodes = testEmptyState.toCache(maxSize = 100L)(hiddenNodes).unsafeRunSync()

  "init" should:
    "create an empty MapCacheState with the given sample count" in newCase[CaseData]: data =>
      MapCacheState.init[IO](10L).logValue.asserting(_ mustEqual data.testEmptyState)

  "toQueryParams" should:
    "return a map with correct query parameters for valid state" in newCase[CaseData]: data =>
      data.testEmptyState.toQueryParams
        .logValue
        .asserting(_ mustEqual Map(PROP_NAME.SAMPLES_COUNT -> 10L.toDbParam))

  "toCache" should:
    "add new hidden nodes in cache" in newCase[CaseData]: data =>
      async[IO]:
        val updatedState = hiddenNodes
          .foldLeft(data.testEmptyState)((state, node) => state.toCache(maxSize = 100L)(List(node)).await)

        updatedState.hiddenNodes.keySet mustEqual hiddenNodes.map(_.id).toSet
        updatedState.samples mustBe empty
        updatedState.sampleCount mustEqual 10L
        updatedState.hnQueue.toList mustEqual hiddenNodes.map(_.id).toList

    "update new hidden nodes in cache" in newCase[CaseData]: data =>
      def isSelected(node: HiddenNode[IO]): Boolean = node.id.value == 5L || node.id.value == 6L

      async[IO]:
        val updatedState = data
          .testState10Nodes.toCache(maxSize = 100L)(hiddenNodes.filter(isSelected))
          .await

        updatedState.hiddenNodes.keySet mustEqual hiddenNodes.map(_.id).toSet

        updatedState.hnQueue.toList mustEqual hiddenNodes
          .filter(n => !isSelected(n)).map(_.id) ++ hiddenNodes.filter(isSelected).map(_.id)

    "remove prev cached nodes if number of cached bigger than maxSize" in newCase[CaseData]: data =>
      async[IO]:
        val node11 = makeAbstractNode(11)
        val updatedState = data.testState10Nodes.toCache(maxSize = 10L)(List(node11)).await

        updatedState.hiddenNodes.keySet mustEqual (hiddenNodes.map(_.id).filter(_.value != 1L).toSet + HnId(11L))
        updatedState.hnQueue.toList mustEqual (hiddenNodes.map(_.id).filter(_.value != 1L).toList :+ HnId(11L))

  "findAndAllocateCached" should:
    "find and update position in cache" in newCase[CaseData]: data =>
      async[IO]:
        val cachedIds = List(HnId(1L), HnId(2L), HnId(3L))
        val notCachedIds = List(HnId(11L), HnId(12L), HnId(13L))

        val (updatedState, foundNodes) = data.testState10Nodes.findAndAllocateCached(cachedIds ++ notCachedIds).await

        updatedState.hnQueue.toList mustEqual (data.testState10Nodes.hnQueue.filterNot(cachedIds.contains) ++ cachedIds)
        foundNodes.map(_.id) mustEqual cachedIds

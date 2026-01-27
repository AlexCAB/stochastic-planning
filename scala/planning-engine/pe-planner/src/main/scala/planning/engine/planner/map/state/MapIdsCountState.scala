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
| created: 2025-12-17 |||||||||||*/

package planning.engine.planner.map.state

import planning.engine.common.values.node.{AbsId, ConId, HnId, HnIndex}
import planning.engine.common.values.sample.SampleId

final case class MapIdsCountState(
    nextHnId: Long,
    nextSampleId: Long,
    nextHnIndexMap: Map[HnId, Long]
):
  lazy val isInit: Boolean = nextHnId == 1L && nextSampleId == 1L && nextHnIndexMap.isEmpty

  private [state] def getNextHnIds[I <: HnId](n: Long, make: Long => I): (MapIdsCountState, List[I]) =
    val hnIds = (nextHnId until (nextHnId + n)).toList.map(make)
    val newState = copy(nextHnId = nextHnId + n)
    (newState, hnIds)

  def getNextConIds(n: Long): (MapIdsCountState, List[ConId]) = getNextHnIds(n, ConId.apply)
  
  def getNextAbsIds(n: Long): (MapIdsCountState, List[AbsId]) = getNextHnIds(n, AbsId.apply)

  def getNextSampleIds(n: Long): (MapIdsCountState, List[SampleId]) =
    val sampleIds = (nextSampleId until (nextSampleId + n)).toList.map(SampleId.apply)
    val newState = copy(nextSampleId = nextSampleId + n)
    (newState, sampleIds)

  def getNextHnIndexes(hnIds: Set[HnId]): (MapIdsCountState, Map[HnId, HnIndex]) =
    val (result, updatedCounts) = hnIds.foldRight((Map[HnId, HnIndex](), nextHnIndexMap)):
      case (hnId, (acc, count)) =>
        val i = count.getOrElse(hnId, 1L)
        (acc + (hnId -> HnIndex(i)), count.updated(hnId, i + 1L))
    (copy(nextHnIndexMap = updatedCounts), result)

object MapIdsCountState:
  lazy val init: MapIdsCountState = MapIdsCountState(
    nextHnIndexMap = Map[HnId, Long](),
    nextHnId = 1L,
    nextSampleId = 1L
  )

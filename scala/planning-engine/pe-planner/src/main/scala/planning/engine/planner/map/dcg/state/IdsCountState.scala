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

package planning.engine.planner.map.dcg.state

import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId

final case class IdsCountState(
    nextHdId: Long,
    nextSampleId: Long,
    nextHnIndexMap: Map[HnId, Long]
):
  def getNextHdIds(n: Long): (IdsCountState, List[HnId]) =
    val hnIds = (nextHdId until (nextHdId + n)).toList.map(HnId.apply)
    val newState = copy(nextHdId = nextHdId + n)
    (newState, hnIds)

  def getNextSampleIds(n: Long): (IdsCountState, List[SampleId]) =
    val sampleIds = (nextSampleId until (nextSampleId + n)).toList.map(SampleId.apply)
    val newState = copy(nextSampleId = nextSampleId + n)
    (newState, sampleIds)

  def getNextHnIndexes(hnIds: List[HnId]): (IdsCountState, Map[HnId, HnIndex]) =
    val (result, updatedCounts) = hnIds.foldRight((Map[HnId, HnIndex](), nextHnIndexMap)):
      case (hnId, (acc, count)) =>
        val i = count.getOrElse(hnId, 1L)
        (acc + (hnId -> HnIndex(i)), count.updated(hnId, i + 1L))
    (copy(nextHnIndexMap = updatedCounts), result)

object IdsCountState:
  lazy val init: IdsCountState = IdsCountState(
    nextHnIndexMap = Map[HnId, Long](),
    nextHdId = 1L,
    nextSampleId = 1L
  )

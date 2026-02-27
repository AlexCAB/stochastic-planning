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

import planning.engine.common.graph.edges.IndexMap
import planning.engine.common.values.node.{MnId, HnIndex}
import planning.engine.common.values.sample.SampleId

final case class MapIdsCountState(
    nextMnId: Long,
    nextSampleId: Long,
    nextHnIndexMap: Map[MnId, Long]
):
  private[state] def nextNId[N <: MnId](n: Long, make: Long => N): (MapIdsCountState, List[N]) =
    val mnIds = (nextMnId until (nextMnId + n)).toList.map(make)
    val newState = this.copy(nextMnId = nextMnId + n)
    (newState, mnIds)

  def getNextConIds(n: Long): (MapIdsCountState, List[MnId.Con]) = nextNId(n, MnId.Con.apply)

  def getNextAbsIds(n: Long): (MapIdsCountState, List[MnId.Abs]) = nextNId(n, MnId.Abs.apply)

  def getNextSampleIds(n: Long): (MapIdsCountState, List[SampleId]) =
    val sampleIds = (nextSampleId until (nextSampleId + n)).toList.map(SampleId.apply)
    val newState = this.copy(nextSampleId = nextSampleId + n)
    (newState, sampleIds)

  def getNextHnIndexes(nIds: Set[MnId]): (MapIdsCountState, IndexMap) =
    val idsMap = nIds.map(mnId => mnId -> nextHnIndexMap.getOrElse(mnId, HnIndex.init.value)).toMap
    val newState = this.copy(nextHnIndexMap = nextHnIndexMap ++ idsMap.map((i, v) => i -> (v + 1L)))
    val indexMap = IndexMap(idsMap.map((i, v) => i -> HnIndex(v)))
    (newState, indexMap)

object MapIdsCountState:
  lazy val init: MapIdsCountState = MapIdsCountState(
    nextHnIndexMap = Map[MnId, Long](),
    nextMnId = 1L,
    nextSampleId = 1L
  )

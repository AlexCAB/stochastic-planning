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

import cats.{MonadThrow, ApplicativeThrow}
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleData
import scala.collection.immutable.Queue
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import cats.syntax.all.*
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId

trait MapCacheLike[F[_]]:
  def sampleCount: Long
  def toQueryParams: F[Map[String, Param]]

final case class MapCacheState[F[_]: MonadThrow](
    hiddenNodes: Map[HnId, HiddenNode[F]],
    samples: Map[SampleId, SampleData],
    sampleCount: Long,
    hnQueue: Queue[HnId]
) extends MapCacheLike[F]:

  override def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.SAMPLES_COUNT -> sampleCount.toDbParam
  )

  def toCache(maxSize: Long)(nodes: List[HiddenNode[F]]): F[MapCacheState[F]] =
    
    // TODO should also update relationships in the cache
    def update(node: HiddenNode[F], cached: Map[HnId, HiddenNode[F]], queue: Queue[HnId]) =
      if cached.contains(node.id)
      then (cached.updated(node.id, node), queue.filter(_ != node.id).enqueue(node.id))
      else (cached + (node.id -> node), queue.enqueue(node.id))

    def clean(cached: Map[HnId, HiddenNode[F]], queue: Queue[HnId]) =
      if queue.size > maxSize
      then
        val (hnId, newQueue) = queue.dequeue
        (cached.removed(hnId), newQueue)
      else (cached, queue)

    def process(nodes: List[HiddenNode[F]]) = nodes.foldLeft((hiddenNodes, hnQueue).pure)((cache, node) =>
      for
        (cached, queue) <- cache
        (newCached, newQueue) = update(node, cached, queue)
        (cleanedCached, cleanedQueue) = clean(newCached, newQueue)
      yield (cleanedCached, cleanedQueue)
    )

    process(nodes).map((newCached, newQueue) => this.copy(hiddenNodes = newCached, hnQueue = newQueue))

  def findAndAllocateCached(ids: List[HnId]): F[(MapCacheState[F], List[HiddenNode[F]])] =
    ApplicativeThrow[F]
      .catchNonFatal(ids.foldLeft((hnQueue, List[HiddenNode[F]]())):
        case ((queue, found), id) => hiddenNodes.get(id) match
            case Some(node) => (queue.filter(_ != id).enqueue(id), node +: found)
            case None       => (queue, found))
      .map((queue, found) => (this.copy(hnQueue = queue), found.reverse))

object MapCacheState:
  def init[F[_]: MonadThrow](sampleCount: Long): F[MapCacheState[F]] = MapCacheState[F](
    hiddenNodes = Map.empty,
    sampleCount = sampleCount,
    samples = Map.empty,
    hnQueue = Queue.empty
  ).pure

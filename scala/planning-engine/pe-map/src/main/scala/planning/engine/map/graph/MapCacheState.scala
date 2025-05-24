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

import cats.MonadThrow
//import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import cats.syntax.all.*
//import planning.engine.map.database.Neo4jQueries.ROOT_LABEL
//import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleData

//import scala.collection.immutable.Queue
//import scala.util.Try

//type StateNodes[F[_]] = (KnowledgeGraphState[F], List[HiddenNode[F]])

trait MapCacheLike[F[_]]:
  def nextHnIdId: HnId
  def nextSampleId: SampleId
  def sampleCount: Long
  def toQueryParams: F[Map[String, Param]]

final case class MapCacheState[F[_]: MonadThrow](
    nextHnIdId: HnId,
    hiddenNodes: Map[HnId, HiddenNode[F]],
    nextSampleId: SampleId,
    sampleCount: Long,
    samples: Map[SampleId, SampleData]
) extends MapCacheLike[F]:
//  private def addNewHn(node: HiddenNode[F]): F[KnowledgeGraphState[F]] =
//    if !hiddenNodes.contains(node.id)
//    then this.copy(nextHnIdId = nextHnIdId.increase, hiddenNodes = hiddenNodes + (node.id -> node)).pure
//    else s"Node with id ${node.id} already exists in the list: $hiddenNodes".assertionError
//
  private[map] def toCache(nodes: List[HiddenNode[F]], maxSize: Long): F[MapCacheState[F]] =
    MonadThrow[F].fromTry(Try:
      val (nodeToRemove, updatedCache) = nodes.foldRight((List[HiddenNode[F]](), cacheQueue)):
        case (node, (toRemove, cache)) if cache.nonEmpty && cache.size > maxSize =>
          val (hnId, newCache) = cache.dequeue
          (hiddenNodes(hnId) :: toRemove, newCache.appended(node.id))
        case (node, (toRemove, cache)) => (toRemove, cache.appended(node.id))
      (this.copy(cacheQueue = updatedCache), nodeToRemove))

  private[map] def toQueryParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.NEXT_HN_ID -> nextHnIdId.toDbParam,
    PROP_NAME.NEXT_SAMPLES_ID -> nextSampleId.toDbParam,
    PROP_NAME.SAMPLES_COUNT -> sampleCount.toDbParam,
  )
  

//
//  private[map] def unCache(id: HnId): F[KnowledgeGraphState[F]] =
//    if hiddenNodes.contains(id)
//    then this.copy(cacheQueue = cacheQueue.filter(_ != id)).pure
//    else s"Invalid state for uncache operation: $id should be in hiddenNodes ($hiddenNodes)".assertionError
//
//  private[map] def addHiddenNode[P, N <: HiddenNode[F]](
//      params: List[P],
//      make: P => F[N]
//  ): F[(KnowledgeGraphState[F], List[N])] = params.foldRight((this, List[N]()).pure):
//    case (param, buffer) =>
//      for
//        (st, acc) <- buffer
//        node <- make(param)
//        nst <- st.addNewHn(node)
//      yield (nst, node :: acc)
//
//  private[map] def releaseHns(ids: List[HnId], maxCacheSize: Long): F[KnowledgeGraphState[F]] =
//    def removeLoop(st: KnowledgeGraphState[F], nodes: List[HiddenNode[F]]): F[KnowledgeGraphState[F]] = nodes match
//      case Nil          => st.copy(hiddenNodes = hiddenNodes.removedAll(nodes.map(_.id))).pure
//      case node :: tail => node.remove(removeLoop(st, tail))
//
//    def releaseLoop(ids: List[HnId], cache: List[HiddenNode[F]]): F[KnowledgeGraphState[F]] = ids match
//      case Nil => toCache(cache, maxCacheSize)
//          .flatMap((st, toRemove) => removeLoop(st, toRemove))
//
//      case id :: tail if hiddenNodes.contains(id) && !cacheQueue.contains(id) =>
//        hiddenNodes(id)
//          .release((node, isZeroUsages) => releaseLoop(tail, if isZeroUsages then node :: cache else cache))
//
//      case id :: _ => s"Not found node with ID $id, n: $hiddenNodes".assertionError
//
//    releaseLoop(ids, List())
//
//  private[map] def findAndAllocateCached(
//      ids: List[HnId],
//      loadNotFound: List[HnId] => F[List[HiddenNode[F]]]
//  ): F[StateNodes[F]] =
//    def loop(
//        ids: List[HnId],
//        notFound: List[HnId],
//        state: KnowledgeGraphState[F]
//    ): F[StateNodes[F]] = ids match
//      case Nil => loadNotFound(notFound)
//          .map(nodes => (state, nodes))
//
//      case id :: tail => hiddenNodes.get(id) match
//          case Some(node) => state.unCache(id).flatMap(upState =>
//              node
//                .allocate[HiddenNode[F], StateNodes[F]](loop(tail, notFound, upState))
//                .map((n, res) => (res._1, n :: res._2))
//            )
//          case None => loop(tail, id :: notFound, state)
//
//    loop(ids, List(), this)
//
object MapCacheState:
  private[map] def init[F[_]: MonadThrow]: F[MapCacheState[F]] = MapCacheState[F](
    nextHnIdId = HnId.init,
    hiddenNodes = Map(),
    nextSampleId = SampleId.init,
    sampleCount = 0L,
    samples = Map()
  ).pure

  private[map] def empty[F[_]: MonadThrow](
      nextHnIdId: HnId,
      nextSampleId: SampleId,
      sampleCount: Long
  ): F[MapCacheState[F]] = MapCacheState[F](
    nextHnIdId,
    hiddenNodes = Map(),
    nextSampleId,
    sampleCount,
    samples = Map()
  ).pure

//
//  private[map] def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[KnowledgeGraphState[F]] =
//    for
//        nextHiddenNodeId <- props.getValue[F, Long](PROP_NAME.NEXT_HN_ID).map(HnId.apply)
//    yield KnowledgeGraphState(nextHiddenNodeId, Map(), Queue())
//
//  private[map] def fromNode[F[_]: MonadThrow](node: Node): F[KnowledgeGraphState[F]] = node match
//    case n if n.is(ROOT_LABEL) => fromProperties[F](n.properties)
//    case _                     => s"Not a root node, $node".assertionError

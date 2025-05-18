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

package planning.engine.map.knowledge.graph

import cats.MonadThrow
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import cats.syntax.all.*
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.database.model.extensions.is
import scala.collection.immutable.Queue

type StateNodes[F[_]] = (KnowledgeGraphState[F], List[HiddenNode[F]])

final case class KnowledgeGraphState[F[_]: MonadThrow](
    nextHnIdId: HnId,
    hnInUse: Map[HnId, HiddenNode[F]],
    hnCached: Map[HnId, HiddenNode[F]],
    cacheQueue: Queue[HnId]
):
//  private[map] def chekNextId: F[Unit] =
//    if hnInUse.contains(nextHnIdId)
//    then s"Seems bug: Next ID $nextHnIdId already in list of nodes: $hnInUse".assertionError
//    else MonadThrow[F].unit

  private[map] def toQueryParams: F[Map[String, Param]] = paramsOf(PROP_NAME.NEXT_HN_ID -> nextHnIdId.toDbParam)

  private[map] def uncache(id: HnId): F[KnowledgeGraphState[F]] =
    if cacheQueue.contains(id) && hnCached.contains(id) && !hnInUse.contains(id)
    then
      this.copy(
        hnInUse = hnInUse + (id -> hnCached(id)),
        hnCached = hnCached - id,
        cacheQueue = cacheQueue.filter(_ != id)
      ).pure
    else
      (s"Invalid state for uncache operation: $id should be in cacheQueue ($cacheQueue) " +
        s"and in hnCached ($hnCached) and not be in hnInUse ($hnInUse)").assertionError

  //  def addNewHn(node: HiddenNode[F]): F[KnowledgeGraphState[F]] =
//    if !hiddenNodes.contains(node.id)
//    then this.copy(nextHnIdId = nextHnIdId.increase, hiddenNodes = hiddenNodes + (node.id -> node)).pure
//    else s"Node with id ${node.id} already exists in the list: $hiddenNodes".assertionError
//
//  def removeHn(node: HiddenNode[F]): F[KnowledgeGraphState[F]] =
//    if !hiddenNodes.contains(node.id)
//    then this.copy(hiddenNodes = hiddenNodes - node.id).pure
//    else s"Node with id ${node.id} not found in the list: $hiddenNodes".assertionError
//
//  def addHns(nodes: List[HiddenNode[F]]): F[KnowledgeGraphState[F]] =
//    val nodesMap = nodes.map(n => n.id -> n).toMap
//
//    if hiddenNodes.keySet.intersect(nodesMap.keySet).isEmpty
//    then this.copy(hiddenNodes = hiddenNodes ++ nodesMap).pure
//    else s"Some nodes already exist in the list: ${hiddenNodes.keySet.intersect(nodesMap.keySet)}".assertionError

  private[map] def findAndAllocateCached(
      ids: List[HnId],
      loadNotFound: List[HnId] => F[List[HiddenNode[F]]]
  ): F[StateNodes[F]] =
    def loop(
        ids: List[HnId],
        notFound: List[HnId],
        state: KnowledgeGraphState[F]
    ): F[StateNodes[F]] = ids match
      case Nil => loadNotFound(notFound)
          .map(nodes => (state, nodes))

      case id :: tail => hnInUse.get(id) match
          case Some(inUseNode) => inUseNode
              .allocate[HiddenNode[F], StateNodes[F]](loop(tail, notFound, state))
              .map((n, r) => (r._1, n :: r._2))

          case None => hnCached.get(id) match
              case Some(cachedNode) =>
                for
                  (n, (st, r)) <- cachedNode.allocate[HiddenNode[F], StateNodes[F]](loop(tail, notFound, state))
                  nst <- st.uncache(id)
                yield (nst, n :: r)

              case None => loop(tail, id :: notFound, state)

    loop(ids, List(), this)

object KnowledgeGraphState:
  def empty[F[_]: MonadThrow]: KnowledgeGraphState[F] = KnowledgeGraphState[F](HnId.init, Map(), Map(), Queue())

  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[KnowledgeGraphState[F]] =
    for
        nextHiddenNodeId <- props.getValue[F, Long](PROP_NAME.NEXT_HN_ID).map(HnId.apply)
    yield KnowledgeGraphState(nextHiddenNodeId, Map(), Map(), Queue())

  def fromNode[F[_]: MonadThrow](node: Node): F[KnowledgeGraphState[F]] = node match
    case n if n.is(ROOT_LABEL) => fromProperties[F](n.properties)
    case _                     => s"Not a root node, $node".assertionError

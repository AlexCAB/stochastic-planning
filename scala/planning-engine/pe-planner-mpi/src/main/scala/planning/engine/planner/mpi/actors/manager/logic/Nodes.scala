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
| created: 27.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

//import cats.syntax.all.*
//import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.manager.data.Message
//import planning.engine.planner.mpi.actors.node.Node
//import planning.engine.common.errors.*
//import planning.engine.planner.mpi.common.data.node.NodeData

private[manager] trait Nodes:
  self: Actor.type =>
  import Message.*

//  protected def addNodes[F[_]: S](dataKit: NodeData.Kit, state: St)(using
//      d: Def,
//      ctx: Ctx,
//  ): F[(Map[MnId, Option[HnName]], St)] =
//    def spawn(rawId: Long, data: NodeData): F[Node] = Node
//      .spawn(data.nodeType.toMnId(rawId), data, d.self, d.visualizer, (bh, n) => ctx.spawn(bh, n))
//
//    for
//      (nodes, newState) <- state.withNewNodes(dataKit, spawn)
//      ids = nodes.map(n => n.mnId -> n.name).toMap
//    yield (ids, newState)
//
//  protected def upsertNodesByName[F[_]: S](data: NodeData.Kit, state: St)(using
//      d: Def,
//      ctx: Ctx,
//  ): F[(Map[MnId, Option[HnName]], St)] =
//    for
//      names <- data.nodes.flatMap(_.name).pure
//      found <- state.findByName(names.toSet)
//      _ <- logInfo("Found exist nodes by names", found)
//      toAdd = data.filterNotByNames(found.values.toSet)
//      (ids, newState) <- addNodes(toAdd, state)
//      _ <- found.keySet.assertContainsNoneOf(ids.keySet, "Found duplicate node IDs for names")
//      allIds = ids ++ found.map((i, n) => i -> Some(n))
//      _ <- d.visualizer.nodesAdded[F](allIds)
//    yield (allIds, newState)
//
//  private[manager] def doAddNodes[F[_]: S](msg: AddNodes, state: St)(using d: Def, ctx: Ctx): F[St] =
//    for
//      (ids, newState) <- addNodes(msg.data, state)
//      _ <- logInfo("[AddNodes] added nodes", ids.view.mapValues(_.repr).toMap)
//      _ <- msg.reply(NodesAdded(ids))
//      _ <- d.visualizer.nodesAdded[F](ids)
//    yield newState
//
//  private[manager] def doUpsertNodesByName[F[_]: S](msg: UpsertNodesByName, state: St)(using d: Def, ctx: Ctx): F[St] =
//    for
//      (ids, newState) <- upsertNodesByName(msg.data, state)
//      _ <- logInfo("[UpsertNodesByName] result nodes", ids)
//      _ <- msg.reply(NodesUpserted(ids))
//    yield newState

  private[manager] def doAddNode[F[_]: S](msg: AddNode, state: St)(using d: Def, ctx: Ctx): F[St] = ???

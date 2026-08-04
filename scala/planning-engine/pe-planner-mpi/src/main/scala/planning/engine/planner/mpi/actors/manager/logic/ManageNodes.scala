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

import cats.syntax.all.*
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.common.errors.*
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.common.data.node.{NodeData, StaticActors}

trait ManageNodes:
  self: ManagerActor.type =>

  private def addNodes[F[_]: S](data: NodeData.Kit, state: St)(using
      d: Def,
      ctx: Ctx,
  ): F[(Map[MnId, Option[HnName]], St)] =
    for
      definitions <- data.toDefinitions(state.nextId, StaticActors())
      nodeRefs = NodeActor.spawn(definitions, (bh, n) => ctx.spawn(bh, n))
      _ <- logInfo("Added new nodes", nodeRefs)
      newState <- state.withNewNodes(nodeRefs)
    yield (nodeRefs.map((r, d) => d.id -> d.data.name), newState)

  private def upsertNodesByName[F[_]: S](data: NodeData.Kit, state: St)(using
      d: Def,
      ctx: Ctx,
  ): F[(Map[MnId, Option[HnName]], St)] =
    for
      names <- data.nodes.flatMap(_.name).pure
      found <- state.findByName(names.toSet)
      _ <- logInfo("Found exist nodes by names", found)
      toAdd = data.filterNotByNames(found.values.toSet)
      (ids, newState) <- addNodes(toAdd, state)
      _ <- found.keySet.assertContainsNoneOf(ids.keySet, "Found duplicate node IDs for names")
      allIds = ids ++ found.map((i, n) => i -> Some(n))
    yield (allIds, newState)

  private[manager] def doAddNodes[F[_]: S](msg: AddNodes, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (ids, newState) <- addNodes(msg.data, state)
      _ <- logInfo("[AddNodes] added nodes", ids.view.mapValues(_.repr).toMap)
      _ <- msg.replay(ManagerAdaptor.NodesAdded(ids))
    yield newState

  private[manager] def doUpsertNodesByName[F[_]: S](msg: UpsertNodesByName, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (ids, newState) <- upsertNodesByName(msg.data, state)
      _ <- logInfo("[UpsertNodesByName] result nodes", ids)
      _ <- msg.replay(ManagerAdaptor.NodesUpserted(ids))
    yield newState

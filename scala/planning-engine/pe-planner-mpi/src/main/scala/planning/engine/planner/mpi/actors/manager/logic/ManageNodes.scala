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
import planning.engine.planner.mpi.data.node.{NodeData, StaticActors}
import planning.engine.common.errors.*

trait ManageNodes:
  self: ManagerActor.type =>

  protected def addNodes[F[_]: S](data: NodeData.Kit, state: St)(using
      d: Def,
      ctx: Ctx,
  ): F[(Map[MnId, Option[HnName]], St)] =
    for
      definitions <- data.toDefinitions(state.nextId, StaticActors())
      nodeRefs = NodeActor.spawn(definitions, (bh, n) => ctx.spawn(bh, n))
      _ <- logInfo("Added new nodes", nodeRefs)
      newState <- state.withNewNodes(nodeRefs)
    yield (nodeRefs.map((r, d) => d.id -> d.data.name), newState)

  protected def upsertNodesByName[F[_]: S](data: NodeData.Kit, state: St)(using
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

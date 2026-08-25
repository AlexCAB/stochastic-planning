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
import cats.syntax.ext.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.common.errors.*

private[manager] trait Nodes:
  self: Actor.type =>
  import Message.*, MnId.Nim

  protected def addNodes[F[_]: S](
      data: Map[Nim, NodeData],
      state: St,
  )(using d: Def, c: Ctx): F[(Map[Nim, Node], St)] = state.withNewNodes(
    data,
    (id, data) => Node.spawn(data.nodeType.toMnId(id), data, d.self, d.visualizer, (bh, n) => c.spawn(bh, n)),
  )

  protected def upsertNodesByName[F[_]: S](
      data: Map[Nim, NodeData],
      state: St,
  )(using d: Def, c: Ctx): F[(Map[Nim, Node], St)] =
    def findNodesByNames: F[(Map[Nim, Node], Map[Nim, NodeData])] = data
      .foldU((Map[Nim, Node](), Map[Nim, NodeData]())):
        case ((nAcc, dAcc), (id, nd)) if nd.name.isDefined =>
          state.findByName(nd.name.get).map:
            case Some(node) => (nAcc + (id -> node), dAcc)
            case None       => (nAcc, dAcc + (id -> nd))
        case ((nAcc, dAcc), (id, nd)) => (nAcc, dAcc + (id -> nd)).pure

    for
      (foundNodes, toAdd) <- findNodesByNames
      _ <- logInfo("Found exist nodes by names", foundNodes)
      (newNodes, newState) <- addNodes(toAdd, state)
      _ <- logInfo("Created new nodes", newNodes)
      _ <- foundNodes.keySet.assertContainsNoneOf(newNodes.keySet, "Found duplicate node Nim's, seems bug")
      foundIds = foundNodes.values.map(_.mnId)
      _ <- foundIds.assertDistinct("Duplicate node IDs in found, seems bug")
      newIds = newNodes.values.map(_.mnId)
      _ <- newIds.assertDistinct("Duplicate node IDs in new, seems bug")
      _ <- foundIds.assertContainsNoneOf(foundIds, "Found duplicate between new and found, seems bug")
      allNodes = foundNodes ++ newNodes
    yield (allNodes, newState)

  private[manager] def doAddNode[F[_]: S](msg: AddNode, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (nodes, newState) <- addNodes(Map(Nim.zero -> msg.data), state)
      node <- nodes.get(Nim.zero).map(_.pure).getOrElse("Node not returned after adding, seems bug".assertionError)
      _ <- logInfo(s"[AddNode] added node $node")
      _ <- msg.reply(NodeAdded(node.mnId))
      _ <- d.visualizer.nodesAdded[F](Map(node.mnId -> node.name))
    yield newState

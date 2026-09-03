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
import planning.engine.common.errors.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData

private[manager] trait Nodes:
  self: Actor.type =>
  import Message.*, MnId.Nim

  protected def addNodes[F[_]: S](
      data: Map[Nim, NodeData],
      state: St,
  )(using d: Def, c: Ctx): F[(Map[Nim, Node], St)] = ifNonEmpty((Map.empty, state), data):
    def spawnNode(rawId: Long, data: NodeData): F[Node] =
      Node.spawn(data.nodeType.toMnId(rawId), data, d.self, d.visualizer, d.planner, (bh, n) => c.spawn(bh, n))

    for
      (newNodes, newState) <- state.withNewNodes(data, spawnNode)
      nodeMap = newNodes.values.map(n => n.mnId -> n).toMap
      _ <- d.visualizer.nodesAdded[F](nodeMap.view.mapValues(_.name).toMap)
      _ <- d.planner.conNodesAdded[F](nodeMap.collect { case (_, node: Node.Con) => node }.toSet)
      _ <- logMap("[addNodes] Created new nodes", newNodes)
    yield (newNodes, newState)

  protected def upsertNodesByName[F[_]: S](
      data: Map[Nim, NodeData],
      state: St,
  )(using d: Def, c: Ctx): F[(Map[Nim, Node], Map[Nim, Node], St)] = ifNonEmpty((Map.empty, Map.empty, state), data):
    def findNodesByNames: F[(Map[Nim, Node], Map[Nim, NodeData])] = data
      .foldM((Map[Nim, Node](), Map[Nim, NodeData]())):
        case ((nAcc, dAcc), (id, nd)) if nd.name.isDefined =>
          state.findByName(nd.name.get).map:
            case Some(node) => (nAcc + (id -> node), dAcc)
            case None       => (nAcc, dAcc + (id -> nd))
        case ((nAcc, dAcc), (id, nd)) => (nAcc, dAcc + (id -> nd)).pure

    for
      (foundNodes, toAdd) <- findNodesByNames
      _ <- logMap("[upsertNodesByName] Found existing nodes by names", foundNodes)
      (newNodes, newState) <- addNodes(toAdd, state)
      _ <- foundNodes.keySet.assertContainsNoneOf(newNodes.keySet, "Found duplicate node Nim's, seems bug")
      allMnIds = foundNodes.values.map(_.mnId) ++ newNodes.values.map(_.mnId)
      _ <- allMnIds.assertDistinct("Duplicate node IDs found, seems bug")
    yield (foundNodes, newNodes, newState)

  private[manager] def doAddNode[F[_]: S](msg: AddNode, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (nodes, newState) <- addNodes(Map(Nim.zero -> msg.data), state)
      node <- nodes.get(Nim.zero).map(_.pure).getOrElse("Node not returned after adding, seems bug".assertionError)
      _ <- logInfo(s"[AddNode] added node $node")
      _ <- msg.reply(NodeAdded(node.mnId))
    yield newState

  private[manager] def doUpsertNodesByName[F[_]: S](msg: UpsertNodesByName, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (foundNodes, newNodes, newState) <- upsertNodesByName(Map(Nim.zero -> msg.data), state)
      allNodes = foundNodes ++ newNodes
      node <- allNodes.get(Nim.zero).map(_.pure).getOrElse("Node not returned, seems bug".assertionError)
      _ <- logInfo(s"[UpsertNodesByName] upserted node $node")
      _ <- msg.reply(NodesByNameUpserted(node.mnId))
    yield newState

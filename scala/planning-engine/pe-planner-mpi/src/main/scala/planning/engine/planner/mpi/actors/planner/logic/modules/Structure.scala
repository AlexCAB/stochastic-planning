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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.logic.modules

import cats.syntax.all.*
import planning.engine.planner.mpi.actors.planner.data.Message.ConNodesAdded
import planning.engine.planner.mpi.actors.planner.logic.Actor

private[planner] trait Structure:
  self: Actor.type =>

  private[planner] def doConNodesAdded[F[_]: S](msg: ConNodesAdded, state: St)(using d: Def, c: Ctx): F[St] =
    for
      (inNodes, outNodes) <- d.variables.conNodesByIo(msg.nodes)
      stWithInNodes = state.mapStruct(_.withNewInNodes(inNodes))
      stWithOutNodes = stWithInNodes.mapStruct(_.withNewOutNodes(outNodes))
      _ <- logSeq("[ConNodesAdded] con nodes added", msg.nodes.map(_.repr))
    yield stWithOutNodes

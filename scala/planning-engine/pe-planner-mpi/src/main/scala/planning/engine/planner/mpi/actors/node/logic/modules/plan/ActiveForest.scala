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
| created: 2026-10-10 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic.modules.plan

import cats.syntax.all.*
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.actors.node.logic.Actor

private[node] trait ActiveForest:
  self: Actor.type =>
  import Message.*

  private def calcInitPu[F[_]: S](msg: ActivationCon, state: St): F[PU] =
    // TODO
    PU(
      p = 1.0, // For active concrete nodes it always 1.0, since they are observed and activated.
      u = 0.0, // TODO: Calculate utility based on the `msg.utility` value and other factors if needed.
    ).pure

  private[node] def doActivationCon[F[_]: S](msg: ActivationCon, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      pu <- calcInitPu(msg, state)
      _ <- logInfo(s"[doActivationCon] time = ${msg.time}, utility = ${msg.utility}, calculated PU = $pu")
      (activeSteps, newState) <- state.mapPlanR(_.withActiveSteps(d.id, msg.time))
      outLinks = state.struct.lookup.outLink.values.toSet
      weights <- HuangWeight.init.div(outLinks.size)
      sampleIds = state.struct.sampleMap.keySet  // For active concrete nodes, we select all sample IDs.

    // TODO
      
    yield newState

  private[node] def doActivationAbs[F[_]: S](msg: ActivationAbs, state: St)(using d: Def, ctx: Ctx): F[St] = ??? // TODO

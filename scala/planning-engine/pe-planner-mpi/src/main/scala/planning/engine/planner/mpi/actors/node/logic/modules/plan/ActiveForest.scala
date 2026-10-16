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
import planning.engine.common.errors.*
import planning.engine.common.errors.assertSameSize
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.logic.Actor
import planning.engine.planner.mpi.actors.node.logic.math.PuCalculation

private[node] trait ActiveForest extends PuCalculation:
  self: Actor.type =>
  import Message.*

  private[node] def doActivationCon[F[_]: S](msg: ActivationCon, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      pu <- calcInitPu(msg, state)
      _ <- logInfo(s"[doActivationCon] time = ${msg.time}, utility = ${msg.utility}, calculated PU = $pu")
      _ <- state.plan.activeSteps.assertEmpty("Active steps must be empty before activation.")
      (activeSteps, newState) <- state.mapPlanR(_.withActiveSteps(d.id, msg.time))
      outLinks <- state.struct.lookup.allOutAbsLink.map(_.toList)
      weights <- HuangWeight.init.div(outLinks.size)
      _ <- logInfo(s"[doActivationCon] steps = $activeSteps, outLinks = $outLinks, weights = $weights")
      _ <- outLinks.assertSameSize(weights, "Number of outLinks and weights must match.")
      paths = outLinks.zip(weights).map((n, w) => n -> ActivePath.init(n, activeSteps, pu, w))
      sampleIds = state.struct.sampleMap.keySet // For active concrete nodes, we select all sample IDs.
      _ <- logInfo(s"[doActivationCon] sampleIds = $sampleIds")
      _ <- paths.traverse_((n, ap) => n.activation(msg.time, sampleIds, ap))
      _ <- logInfo(s"[doActivationCon] Sent ${outLinks.size} activation messages to outLinks.")
    yield newState

  private[node] def   doActivationAbs[F[_]: S](msg: ActivationAbs, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      pu <- calcNextPu(msg, state)
      _ <- logInfo(s"[doActivationAbs] time = ${msg.time}, sampleIds = ${msg.sampleIds}, path = ${msg.path}")
      outLinks <- state.struct.lookup.findOutAbsLink(msg.sampleIds).map(_.toList)
      
      
      
//      (activeSteps, newState) <- state.mapPlanR(_.withActiveSteps(d.id, msg.time))
//      
//      
//      outLinks <- state.struct.lookup.outAbsLink.map(_.values.toList)
//      weights <- HuangWeight.init.div(outLinks.size)
//      _ <- logInfo(s"[doActivationAbs] steps = $activeSteps, outLinks = $outLinks, weights = $weights")
//      _ <- outLinks.assertSameSize(weights, "Number of outLinks and weights must match.")
//      paths = outLinks.zip(weights).map((n, w) => n -> ActivePath.init(n, activeSteps, pu, w))
//      sampleIds = state.struct.sampleMap.keySet // For active concrete nodes, we select all sample IDs.
//      _ <- logInfo(s"[doActivationAbs] sampleIds = $sampleIds")
//      _ <- paths.traverse_((n, ap) => n.activation(msg.time, sampleIds, ap))
//      _ <- logInfo(s"[doActivationAbs] Sent ${outLinks.size} activation messages to outLinks.")
    yield ??? //newState
    
    

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
| created: 2026-10-16 |||||||||||*/



package planning.engine.planner.mpi.actors.node.logic.math

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.common.graph.inference.PU
import planning.engine.planner.mpi.actors.node.data.Message

private[node] trait PuCalculation:
  import Message.*
  
  private[node] def calcInitPu[F[_] : MT](msg: ActivationCon): F[PU] =
    // TODO
    PU(
      p = 1.0, // For active concrete nodes it always 1.0, since they are observed and activated.
      u = 0.0, // TODO: Calculate utility based on the `msg.utility` value and other factors if needed.
    ).pure
  
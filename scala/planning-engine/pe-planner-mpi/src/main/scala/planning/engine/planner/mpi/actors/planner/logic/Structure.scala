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

package planning.engine.planner.mpi.actors.planner.logic

import planning.engine.planner.mpi.actors.planner.data.Message.ConNodeAdded

private[planner] trait Structure:
  self: Actor.type =>

  // TODO Not yet implemented, will register the new concrete node into the input/output nodes state.
  private[planner] def doConNodeAdded[F[_]: S](msg: ConNodeAdded, state: St)(using Def, Ctx): F[St] = ???

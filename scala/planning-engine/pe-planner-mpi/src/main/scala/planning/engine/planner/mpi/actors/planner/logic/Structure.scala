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

import planning.engine.planner.mpi.actors.planner.data.Message.ConNodesAdded

private[planner] trait Structure:
  self: Actor.type =>

  private[planner] def doConNodesAdded[F[_]: S](msg: ConNodesAdded, state: St)(using Def, Ctx): F[St] = ???

//
//    for
//      _ <- validateIds
//    yield ???

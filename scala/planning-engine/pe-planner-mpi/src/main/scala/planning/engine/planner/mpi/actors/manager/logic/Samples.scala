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
| created: 17-Aug-26 |||||||||||*/



package planning.engine.planner.mpi.actors.manager.logic

import planning.engine.planner.mpi.actors.manager.data.Message

private[manager] trait Samples extends Nodes with Edges:
  self: Actor.type =>
  import Message.*
  
  def doAddManSamples[F[_]: S](msg: AddManSamples, state: St)(using d: Def, ctx: Ctx): F[St] = ???

  def doAddGenSamples[F[_]: S](msg: AddGenSamples, state: St)(using d: Def, ctx: Ctx): F[St] = ???
     
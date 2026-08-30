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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic

import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.actors.node.data.*

private[node] object Actor extends ActorBase with Structure:
  import Message.*, ActorBase.GetState

  override type Def = Definition
  override type Msg = Message | GetState[St]

  override protected type St = (StructState, PlanState)

  extension [F[_]: S](s: St)
    protected def struct: StructState = s._1
    protected def mapStruct(f: StructState => F[StructState]): F[St] = f(s._1).map(ns => (ns, s._2))
    protected def plan: PlanState = s._2
    protected def mapPlan(f: PlanState => F[PlanState]): F[St] = f(s._2).map(np => (s._1, np))

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(s"map-node-actor-${d.id}")

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: UpsertEdgeSrc => doUpsertEdgeSrc(msg, state)
    case msg: UpsertEdgeTrg => doUpsertEdgeTrg(msg, state)
    case msg: GetState[St]  => doGetState(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using d: Def, c: Ctx): F[St] =
    d.actors.manager.reportError[F](d.self, Some(msg), err).as(state)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref =
    make(apply(definition, (StructState.init, PlanState.init)), definition.id.value.toString)

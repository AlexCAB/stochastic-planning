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
| created: 2026-09-14 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian.logic

import cats.syntax.all.*
import planning.engine.common.errors.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Terminated}
import planning.engine.planner.mpi.actors.guardian.data.Message
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer

private[guardian] trait Lifecycle:
  self: Actor.type =>
  import Message.*

  private[guardian] def doInitialize[F[_]: S](msg: Initialize)(using ctx: Ctx): F[Bhv] =
    def makeViz = msg.visualization match
      case Some(v) => Visualizer.spawn(v, ctx).map(Some(_))
      case None    => None.pure

    for
      _ <- ctx.children.assertEmpty("Cannot initialize Guardian, it is already initialized or not reset")
      visualizer <- makeViz
      planner <- Planner.spawn(msg.inVars, msg.outVars, ctx)
      manager <- Manager.spawn(visualizer, planner, ctx)
      _ <- logInfo(s"Created actors: $visualizer, $planner, $manager")
      _ <- msg.reply(Initialized(manager, planner, visualizer))
    yield Behaviors.same

  private[guardian] def doReset[F[_]: S](msg: Reset)(using ctx: Ctx): F[Bhv] =
    def cleaned: Bhv =
      ctx.log.info("All child actors cleaned")
      msg.sender ! Cleaned
      Behaviors.same

    def awaitNext(children: Set[ActorRef[Nothing]]): Bhv = Behaviors.receiveSignal[Msg]:
      case (_, Terminated(child)) =>
        ctx.log.info(s"Terminated child actor: $child")
        val remaining = children - child
        if remaining.isEmpty then cleaned else awaitNext(remaining)

    for
      children <- delay(ctx.children)
      _ = children.foreach(ctx.watch)
      _ = children.foreach(ctx.stop)
    yield if children.isEmpty then cleaned else awaitNext(children.toSet)

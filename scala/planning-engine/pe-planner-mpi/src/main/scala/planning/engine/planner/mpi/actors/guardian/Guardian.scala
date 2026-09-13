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
| created: 2026-09-09 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian

import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior, Terminated}
import planning.engine.planner.mpi.actors.Stateless
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer

private[mpi] object Guardian extends Stateless with Messages:
  val name = "map-guardian-actor"

  override protected def setup()(using ctx: Ctx): Unit = ctx.setLoggerName(name)

  private def doInitialize[F[_]: S](msg: Initialize)(using ctx: Ctx): F[Unit] =
    def makeViz = msg.visualization match
      case Some(v) => Visualizer.spawn(v, (b, n) => ctx.spawn(b, n)).map(Some(_))
      case None    => None.pure

    for
      visualizer <- makeViz
      planner <- Planner.spawn(msg.inVars, msg.outVars, (b, n) => ctx.spawn(b, n))
      manager <- Manager.spawn(visualizer, planner, (b, n) => ctx.spawn(b, n))
      _ <- logInfo(s"Created actors: $visualizer, $planner, $manager")
      _ <- msg.reply(Initialized(manager, planner, visualizer))
    yield ()

  private def awaitingReset(children: Set[ActorRef[Nothing]]): Unit = if children.nonEmpty then
    Behaviors.receiveSignal:
      case (_, Terminated(child)) =>
        awaitingReset(children - child)
        Behaviors.same

  private def doReset[F[_]: S](msg: Reset)(using ctx: Ctx): F[Unit] =
    for
      children <- delay(ctx.children)
      _ = children.foreach(ctx.watch)
      _ = children.foreach(ctx.stop)
      _ = awaitingReset(children.toSet)
      _ <- logInfo(s"All child actors reset, children: ${children.mkString(", ")}")
      _ <- msg.reply(Cleaned)
    yield ()

  override protected def receive[F[_]: S](msg: Msg)(using Ctx): F[Unit] = msg match
    case msg: Initialize => doInitialize(msg)
    case msg: Reset      => doReset(msg)

  def spawn(make: (Behavior[Msg], String) => Ref): Ref = make(apply(), name)

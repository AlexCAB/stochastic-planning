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
| created: .08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.actor.typed.{ActorRef, Scheduler}
import org.apache.pekko.util.Timeout

import scala.concurrent.duration.*

abstract class ApiBase[M](scheduler: Scheduler):
  given Timeout = Timeout(5.seconds)
  given Scheduler = scheduler

  extension (ref: ActorRef[M])
    protected def tellF[F[_]: MonadThrow](msg: M): F[Unit] = MonadThrow[F].catchNonFatal(ref ! msg).void

    protected def askF[F[_]: Async, R](makeMsg: ActorRef[R] => M): F[R] =
      Async[F].fromFuture(Async[F].delay(ref.ask(makeMsg)))

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
| created: 09-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout
import scala.concurrent.duration.*

trait ApiBase[M]:
  given ASK_TIMEOUT: Timeout = Timeout(5.seconds)

  extension (ref: ActorRef[M])
    protected def tellF[F[_]: MonadThrow](msg: M): F[Unit] = MonadThrow[F].catchNonFatal(ref ! msg).void

    protected def askF[F[_]: Async, R](makeMsg: ActorRef[R] => M)(using ActorSystem[?]): F[R] =
      Async[F].fromFuture(Async[F].delay(ref.ask(makeMsg)))

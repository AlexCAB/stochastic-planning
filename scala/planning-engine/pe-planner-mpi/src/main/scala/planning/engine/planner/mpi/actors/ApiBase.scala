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
import cats.syntax.all.*
import org.apache.pekko.actor.typed.ActorRef

trait ApiBase[M]

object ApiBase:
  extension [M](ref: ActorRef[M])
    def tellF[F[_]: MonadThrow](msg: M): F[Unit] = MonadThrow[F].catchNonFatal(ref ! msg).void

    // TODO Implement askF method using Pekko's ask pattern and MonadThrow for error handling.
    def askF[F[_]: MonadThrow, R](msg: M): F[R] = ???

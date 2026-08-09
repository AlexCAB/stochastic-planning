///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 05-Aug-26 |||||||||||*/
//
//package planning.engine.planner.mpi.common.actor
//
//import cats.MonadThrow
//import cats.syntax.all.*
//import org.apache.pekko.actor.typed.ActorRef
//
//object ActorRefEx:
//  extension [T](ref: ActorRef[T])
//    def send[F[_]: MonadThrow](msg: T): F[Unit] = MonadThrow[F].catchNonFatal(ref ! msg).void

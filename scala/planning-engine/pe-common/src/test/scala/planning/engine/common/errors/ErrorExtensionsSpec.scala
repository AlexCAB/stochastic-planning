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
//| created: 2025-03-21 |||||||||||*/
//
//
//package planning.engine.common.errors
//
//import cats.ApplicativeThrow
//import cats.effect.IO
//import cats.effect.testing.specs2.CatsEffect
//import org.specs2.mutable.Specification
//
//
//class ErrorExtensionsSpec extends Specification with CatsEffect:
//
//  "assertionError" should:
//    "raise an AssertionError with the given message" in:
//      val errorMessage = "This is an assertion error"
//      
//      errorMessage.assertionError[IO, Unit].attempt.flatMap:
//        case Left(e: AssertionError) => IO.pure(e.getMessage must beEqualTo(errorMessage)).map(_.toResult)
//        case Left(_) => IO.pure(failure("Not AssertionError or message not matched"))
//        case Right(_) => IO.pure(success)

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
| created: 10.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.IORuntime
import com.typesafe.scalalogging.Logger
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.{Assertion, BeforeAndAfterAll, Outcome}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpecLike

import scala.reflect.ClassTag

class UnitSpecWithTestKit extends FixtureAnyWordSpecLike with Matchers with BeforeAndAfterAll:
  trait Case

  type FixtureParam = OneArgTest

  private def makeLogger(param: FixtureParam): Logger = Logger(param.name)

  def newCase[C <: Case](test: (Logger, C) => Assertion)(implicit m: ClassTag[C]): FixtureParam => Assertion = param =>
    m.runtimeClass.getDeclaredConstructors.toList match
      case c :: Nil if c.getParameterCount == 0 => test(makeLogger(param), c.newInstance().asInstanceOf[C])
      case c :: Nil if c.getParameterCount == 1 => test(makeLogger(param), c.newInstance(this).asInstanceOf[C])
      case _                                    => fail("Invalid constructor found for test case class")

  override def withFixture(test: OneArgTest): Outcome = super.withFixture(test.toNoArgTest(test))

  given IORuntime = IORuntime.global
  private val (dispatcher, dispatcherCancel) = Dispatcher.parallel[IO].allocated.unsafeRunSync()
  given Dispatcher[IO] = dispatcher

  val testKit = ActorTestKit()

  override def afterAll(): Unit =
    testKit.shutdownTestKit()
    dispatcherCancel.unsafeRunSync()

  extension (logger: Logger)
    def msg[M](m: M, comment: String = ""): M =
      logger.info(s"${if comment.nonEmpty then comment else "Message"}: $m")
      m

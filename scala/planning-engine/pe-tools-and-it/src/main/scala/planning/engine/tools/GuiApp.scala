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
| created: 2026-10-23 |||||||||||*/

package planning.engine.tools

import cats.effect.std.Dispatcher
import cats.effect.{Deferred, ExitCode, IO, IOApp, Resource}
import javafx.application.Platform
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.GuiApp.Exit

trait GuiApp extends IOApp:
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  protected def build(args: List[String], exit: Exit[IO])(using Dispatcher[IO]): Resource[IO, Unit]

  private def init(args: List[String]): Resource[IO, Exit[IO]] =
    def initPlatform: IO[Unit] = IO.async_[Unit]: cb =>
      Platform.setImplicitExit(false)
      Platform.startup(() => cb(Right(())))

    def callBuild(exit: Exit[IO]): Resource[IO, Unit] = Dispatcher.parallel[IO](await = false).flatMap: dispatcher =>
      given Dispatcher[IO] = dispatcher
      build(args, exit)

    for
      _ <- Resource.make(initPlatform)(_ => IO(Platform.exit()))
      exit <- Resource.eval(Deferred[IO, Unit])
      _ <- callBuild(exit)
    yield exit

  override def run(args: List[String]): IO[ExitCode] = init(args).use(exit => exit.get).as(ExitCode.Success)

object GuiApp:
  type Exit[F[_]] = Deferred[F, Unit]

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
import cats.effect.{Deferred, IO, IOApp, Resource}
import javafx.application.Platform
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.LoggerFactory

trait GuiApp extends IOApp.Simple:
  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  
  protected def build(closed: Deferred[IO, Unit])(using Dispatcher[IO]): Resource[IO, Unit]

  private def init: Resource[IO, Deferred[IO, Unit]] =
    def initPlatform: IO[Unit] = IO.async_[Unit]: cb =>
      Platform.setImplicitExit(false)
      Platform.startup(() => cb(Right(())))

    def callBuild(closed: Deferred[IO, Unit]): Resource[IO, Unit] =
      Dispatcher.parallel[IO](await = false).flatMap: dispatcher =>
        
        given Dispatcher[IO] = dispatcher
        build(closed)

    for
      _ <- Resource.make(initPlatform)(_ => IO(Platform.exit()))
      closed <- Resource.eval(Deferred[IO, Unit])
      _ <- callBuild(closed)
    yield closed

  override def run: IO[Unit] = init.use(closed => closed.get)

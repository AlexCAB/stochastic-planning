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

package planning.engine.tools.mpi.vizedit.adaptors

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.mpi.MapMpi

// Adopt commands send from GUI to map network `MapMpi`
class ControlAdaptor[F[_]: {Async, Dispatcher, LoggerFactory}](map: MapMpi[F]):
  private val logger = LoggerFactory[F].getLogger

object ControlAdaptor:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](map: MapMpi[F]): Resource[F, ControlAdaptor[F]] =
    Resource.eval(new ControlAdaptor(map).pure)

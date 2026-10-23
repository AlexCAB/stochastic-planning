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

package planning.engine.tools.mpi.vizedit.config

import cats.effect.{Sync, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.gui.Gui

class ConfigWriter[F[_]: {Sync, LoggerFactory}](gui: Gui[F]):
  private val logger = LoggerFactory[F].getLogger

  def write(): F[Unit] = logger.warn("TODO: implement writing config to file")

object ConfigWriter:
  def apply[F[_]: {Sync, LoggerFactory}](gui: Gui[F]): Resource[F, ConfigWriter[F]] = Resource
    .make(Sync[F].delay(new ConfigWriter(gui)))(_.write())

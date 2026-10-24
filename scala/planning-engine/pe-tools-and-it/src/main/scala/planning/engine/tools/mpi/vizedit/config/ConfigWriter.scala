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

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import io.circe.syntax.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.config.model.GuiConf
import planning.engine.tools.mpi.vizedit.gui.Gui

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}

class ConfigWriter[F[_]: {Sync, LoggerFactory}](gui: Gui[F]):
  private val logger = LoggerFactory[F].getLogger

  // Writes to a temp file first, then replaces the config file, so a crash mid-write can't corrupt it.
  private def writeFile(path: Path, json: String): F[Unit] = Sync[F].blocking:
    val tmp = path.resolveSibling(s"${path.getFileName}.tmp")
    Files.writeString(tmp, json, StandardCharsets.UTF_8)
    Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)

  // Saves the current GUI config to the JSON file, read back by `ConfigReader.readOrDefault`.
  def write(): F[Unit] =
    val writeConf =
      for
        conf <- gui.getConfig
        path <- Sync[F].delay(GuiConf.filePath)
        _ <- writeFile(path, conf.asJson.spaces2)
        _ <- logger.info(s"GUI config written to: $path")
      yield ()

    writeConf.handleErrorWith(err => logger.error(err)("Failed to write GUI config"))

object ConfigWriter:
  def apply[F[_]: {Sync, LoggerFactory}](gui: Gui[F]): Resource[F, ConfigWriter[F]] = Resource
    .make(Sync[F].delay(new ConfigWriter(gui)))(_.write())

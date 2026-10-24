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
import io.circe.parser.decode
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.config.model.GuiConf

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

object ConfigReader:

  // Reads the GUI config from the JSON file; if the file doesn't exist or can't be read/parsed, returns the default.
  def readOrDefault[F[_]: {Sync, LoggerFactory}](): Resource[F, GuiConf] =
    val logger = LoggerFactory[F].getLogger

    def readFile(path: Path): F[GuiConf] =
      for
        json <- Sync[F].blocking(Files.readString(path, StandardCharsets.UTF_8))
        conf <- Sync[F].fromEither(decode[GuiConf](json))
        _ <- logger.info(s"GUI config read from: $path")
      yield conf

    def useDefault(path: Path): F[GuiConf] =
      logger.info(s"GUI config file not found: $path, using default config").as(GuiConf.default)

    val readConf: F[GuiConf] =
      for
        path <- Sync[F].delay(GuiConf.filePath)
        exists <- Sync[F].blocking(Files.exists(path))
        conf <- if exists then readFile(path) else useDefault(path)
      yield conf

    Resource.eval(readConf.handleErrorWith: err =>
      logger.warn(err)(s"Failed to read GUI config, using default config").as(GuiConf.default))


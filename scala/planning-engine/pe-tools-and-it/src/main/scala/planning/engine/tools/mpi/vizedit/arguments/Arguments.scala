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
| created: 2026-10-25 |||||||||||*/

package planning.engine.tools.mpi.vizedit.arguments

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import scopt.OParser

import java.nio.file.{Path, Paths}

final case class Arguments(yedPath: Option[Path])

object Arguments:
  val empty: Arguments = Arguments(yedPath = None)

  private val parser: OParser[Unit, Arguments] =
    val builder = OParser.builder[Arguments]
    import builder.*

    OParser.sequence(
      programName("viz-edit-tool"),
      head("Map Network Visual Edit Tool"),
      opt[String]("yedpath")
        .optional()
        .valueName("<path>")
        .text("path to the yEd graph (*.graphml) file to load")
        .action((path, args) => args.copy(yedPath = Some(Paths.get(path)))),
      help("help").text("prints this usage text"),
    )

  // scopt prints the parsing errors and usage to stderr itself, so only the failure is raised here.
  def parse[F[_]: {Sync, LoggerFactory}](args: List[String]): Resource[F, Arguments] =
    val logger = LoggerFactory[F].getLogger

    val parsed: F[Arguments] =
      for
        result <- Sync[F].delay(OParser.parse(parser, args, empty))
        arguments <- result.liftTo[F](IllegalArgumentException(s"Invalid command line arguments: $args"))
        _ <- logger.info(s"Parsed command line arguments: $arguments")
      yield arguments

    Resource.eval(parsed)

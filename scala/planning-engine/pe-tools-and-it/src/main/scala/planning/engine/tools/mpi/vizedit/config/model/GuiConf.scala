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

package planning.engine.tools.mpi.vizedit.config.model

import io.circe.{Decoder, Encoder}
import planning.engine.tools.mpi.vizedit.gui.components.Window
import planning.engine.tools.mpi.vizedit.gui.windows.{Main, Planning, Samples}

import java.nio.file.{Path, Paths}
import scala.util.Try

final case class GuiConf(
    main: Main.Config,
    samples: Samples.Config,
    planning: Planning.Config,
)

object GuiConf:
  import io.circe.generic.semiauto.*, Window.defaultConf

  implicit val windowConfigDecoder: Decoder[Window.Config] = deriveDecoder[Window.Config]
  implicit val windowConfigEncoder: Encoder[Window.Config] = deriveEncoder[Window.Config]

  implicit val pathDecoder: Decoder[Path] = Decoder.decodeString.emapTry(str => Try(Paths.get(str)))
  implicit val pathEncoder: Encoder[Path] = Encoder.encodeString.contramap(_.toString)

  implicit val mainConfigDecoder: Decoder[Main.Config] = deriveDecoder[Main.Config]
  implicit val mainConfigEncoder: Encoder[Main.Config] = deriveEncoder[Main.Config]

  implicit val samplesConfigDecoder: Decoder[Samples.Config] = deriveDecoder[Samples.Config]
  implicit val samplesConfigEncoder: Encoder[Samples.Config] = deriveEncoder[Samples.Config]

  implicit val planningConfigDecoder: Decoder[Planning.Config] = deriveDecoder[Planning.Config]
  implicit val planningConfigEncoder: Encoder[Planning.Config] = deriveEncoder[Planning.Config]

  implicit val decoder: Decoder[GuiConf] = deriveDecoder[GuiConf]
  implicit val encoder: Encoder[GuiConf] = deriveEncoder[GuiConf]

  // Config file in the application (working) directory.
  val fileName: String = "viz-edit-gui.json"

  def appDir: Path = Paths.get(System.getProperty("user.dir"))

  def filePath: Path = appDir.resolve(fileName)

  // Used when no config file exists yet: windows of the default size, cascaded from the top-left corner,
  // the Samples and Planning windows hidden, and the application directory as work directory.
  def default: GuiConf = GuiConf(
    main = Main.Config(window = defaultConf(50), workDir = appDir),
    samples = Samples.Config(isShown = false, window = defaultConf(100)),
    planning = Planning.Config(isShown = false, window = defaultConf(150)),
  )

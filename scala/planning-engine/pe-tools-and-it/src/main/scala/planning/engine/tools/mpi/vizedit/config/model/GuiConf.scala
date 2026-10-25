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
import planning.engine.tools.mpi.vizedit.gui.windows.Main

import java.nio.file.{Path, Paths}
import scala.util.Try

final case class GuiConf(
    main: Main.Config,
    samples: Window.Config,
    planning: Window.Config,
)

object GuiConf:
  import io.circe.generic.semiauto.*, Window.defaultConf

  implicit val windowConfigDecoder: Decoder[Window.Config] = deriveDecoder[Window.Config]
  implicit val windowConfigEncoder: Encoder[Window.Config] = deriveEncoder[Window.Config]

  implicit val pathDecoder: Decoder[Path] = Decoder.decodeString.emapTry(str => Try(Paths.get(str)))
  implicit val pathEncoder: Encoder[Path] = Encoder.encodeString.contramap(_.toString)

  implicit val mainConfigDecoder: Decoder[Main.Config] = deriveDecoder[Main.Config]
  implicit val mainConfigEncoder: Encoder[Main.Config] = deriveEncoder[Main.Config]

  implicit val decoder: Decoder[GuiConf] = deriveDecoder[GuiConf]
  implicit val encoder: Encoder[GuiConf] = deriveEncoder[GuiConf]

  // Config file in the application (working) directory.
  val fileName: String = "viz-edit-gui.json"

  def appDir: Path = Paths.get(System.getProperty("user.dir"))

  def filePath: Path = appDir.resolve(fileName)

  // Used when no config file exists yet: windows of the default size, cascaded from the top-left corner,
  // and the application directory as work directory.
  def default: GuiConf = GuiConf(
    main = Main.Config(window = defaultConf(50), workDir = appDir),
    samples = defaultConf(100),
    planning = defaultConf(150),
  )

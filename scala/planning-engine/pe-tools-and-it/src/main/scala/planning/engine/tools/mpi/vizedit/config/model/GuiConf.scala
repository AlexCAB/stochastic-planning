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

import java.nio.file.{Path, Paths}

final case class GuiConf(
    main: Window.Config,
    samples: Window.Config,
    planning: Window.Config,
)

object GuiConf:
  import io.circe.generic.semiauto.*

  implicit val windowConfigDecoder: Decoder[Window.Config] = deriveDecoder[Window.Config]
  implicit val windowConfigEncoder: Encoder[Window.Config] = deriveEncoder[Window.Config]

  implicit val decoder: Decoder[GuiConf] = deriveDecoder[GuiConf]
  implicit val encoder: Encoder[GuiConf] = deriveEncoder[GuiConf]

  // Config file in the application (working) directory.
  val FileName: String = "viz-edit-gui.json"
  def filePath: Path = Paths.get(System.getProperty("user.dir"), FileName)

  // Used when no config file exists yet: windows of the default size, cascaded from the top-left corner.
  val default: GuiConf =
    def window(offset: Double): Window.Config = Window.Config(offset, offset, Window.DefaultWidth, Window.DefaultHeight)

    GuiConf(main = window(50), samples = window(100), planning = window(150))

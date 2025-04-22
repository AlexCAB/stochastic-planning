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
| created: 2025-04-20 |||||||||||*/

package planning.engine.api.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.*

object ApiConfigReaders:
  given HostReader: ConfigReader[Host] = ConfigReader.fromString[Host](
    ConvertHelpers.catchReadError(s => Host.fromString(s).getOrElse(throw new AssertionError(s"Invalid host: $s")))
  )

  given PortReader: ConfigReader[Port] = ConfigReader.fromString[Port](
    ConvertHelpers.catchReadError(s => Port.fromInt(s.toInt).getOrElse(throw new AssertionError(s"Invalid port: $s")))
  )

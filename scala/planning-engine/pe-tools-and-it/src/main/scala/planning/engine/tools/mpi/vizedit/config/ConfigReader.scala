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
import planning.engine.tools.mpi.vizedit.config.model.GuiConf

object ConfigReader:
  def readOrDefault[F[_]: Sync](): Resource[F, GuiConf] = Resource.pure(GuiConf()) // TODO: implement reading config from file
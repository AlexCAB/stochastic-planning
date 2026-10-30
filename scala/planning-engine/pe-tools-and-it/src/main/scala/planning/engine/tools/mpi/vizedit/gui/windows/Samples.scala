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

package planning.engine.tools.mpi.vizedit.gui.windows

import cats.effect.Async
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import planning.engine.tools.mpi.vizedit.gui.components.{NetworkGraph, SamplesTable, Window}
import planning.engine.tools.mpi.vizedit.gui.controls.SamplesControl

final case class Samples[F[_]: {Async, Dispatcher}](
    graph: NetworkGraph[F],
    samplesTable: SamplesTable[F],
    control: SamplesControl[F],
    window: Window[F],
):
  def getConfig: F[Samples.Config] =
    for
      isShown <- window.isShown
      window <- window.getConfig
    yield Samples.Config(isShown = isShown, window = window)

object Samples:
  final case class Config(
      isShown: Boolean,
      window: Window.Config,
  )

  def apply[F[_]: {Async, Dispatcher}](config: Config): F[Samples[F]] =
    for
      graph <- NetworkGraph[F]()
      samplesTable <- SamplesTable[F]()
      control <- SamplesControl[F](samplesTable)
      window <- Window[F]("Samples", config.window, graph, control, Async[F].unit)
    yield new Samples(graph, samplesTable, control, window)

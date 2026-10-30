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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.gui.components.{SamplesTable, Window}
import planning.engine.tools.mpi.vizedit.gui.controls.SamplesControl
import planning.engine.tools.mpi.vizedit.gui.visualization.SampleGraph

class Samples[F[_]: {Async, Dispatcher}](
    val sampleGraph: SampleGraph[F],
    val samplesTable: SamplesTable[F],
    val control: SamplesControl[F],
    val window: Window[F],
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

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](config: Config): F[Samples[F]] =
    for
      sampleGraph <- SampleGraph[F]()
      samplesTable <- SamplesTable[F]()
      control <- SamplesControl[F](sampleGraph, samplesTable)
      window <- Window[F]("Samples", config.window, sampleGraph.graph, control, Async[F].unit)
    yield new Samples(sampleGraph, samplesTable, control, window)

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

package planning.engine.tools.mpi.vizedit.adaptors

import cats.effect.{Async, Resource}
import cats.effect.std.{AtomicCell, Dispatcher}
import cats.syntax.ext.MT
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.visualization.{ActiveForest, SampleViz}
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.tools.mpi.vizedit.gui.Gui

// Adopt visualization data for map network `MapMpi` to GUI
class VisualizerAdaptor[F[_]: {Async, Dispatcher, LoggerFactory}](gui: AtomicCell[F, Option[Gui[F]]])
    extends Visualization:

  private val logger = LoggerFactory[F].getLogger

  private def withGui[G[_]: MT](event: String)(fa: Gui[F] => F[Unit]): G[Unit] = MT[G, Unit]:
    summon[Dispatcher[F]].unsafeRunAndForget:
      gui.get.flatMap:
        case Some(gui) => fa(gui)
        case None      => logger.warn(s"GUI is not set yet, skip visualization of: $event")

  def setGui(gui: Gui[F]): F[Unit] = this.gui.set(Some(gui))

  def reset(): F[Unit] = withGui("reset"): gui =>
    for
      _ <- gui.main.window.setTitle(None)
      _ <- gui.main.ioVarsTable.clear
      _ <- gui.samples.control.reset
      _ <- List(gui.main.graph.clear, gui.samples.graph.clear, gui.planning.graph.clear).sequence_
      _ <- logger.info(s"Visualizer GUI reset")
    yield ()

  override def init[G[_]: MT](metadata: Metadata, variables: IoVars): G[Unit] = withGui("init"): gui =>
    for
      _ <- gui.main.window.setTitle(Some(metadata.name.value))
      _ <- gui.main.ioVarsTable.update(variables)
      _ <- logger.info(s"GUI initialized for map: $metadata, variables: $variables")
    yield ()

  override def nodesAdded[G[_]: MT](ids: Map[MnId, NodeData]): G[Unit] = MT.void[G]

  override def edgesAdded[G[_]: MT](keys: Set[MeKey]): G[Unit] = MT.void[G]

  override def samplesAdded[G[_]: MT](samples: Set[SampleViz]): G[Unit] = withGui("samplesAdded"): gui =>
    for
      _ <- gui.samples.control.samplesAdded(samples)
      _ <- logger.info(s"GUI samples added: ${samples.map(_.id)}")
    yield ()

  override def activeStepsFound[G[_]: MT](time: IoTime, forest: ActiveForest): G[Unit] = MT.void[G]

object VisualizerAdaptor:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](): Resource[F, VisualizerAdaptor[F]] = Resource.eval:
    for
        gui <- AtomicCell[F].of[Option[Gui[F]]](None)
    yield new VisualizerAdaptor(gui)

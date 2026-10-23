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
import cats.effect.std.Dispatcher
import cats.syntax.ext.MT
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.visualization.ActiveForest
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.tools.mpi.vizedit.gui.visualization.Graphs

// Adopt visualization data for map network `MapMpi` to GUI
class VisualizerAdaptor[F[_]: {Async, Dispatcher, LoggerFactory}](graphs: Graphs[F]) extends Visualization:

  def init[G[_]: MT](metadata: Metadata, variables: IoVars): G[Unit] = MT.void[G]

  def nodesAdded[G[_]: MT](ids: Map[MnId, NodeData]): G[Unit] = MT.void[G]

  def edgesAdded[G[_]: MT](keys: Set[MeKey]): G[Unit] = MT.void[G]

  def activeStepsFound[G[_]: MT](time: IoTime, forest: ActiveForest): G[Unit] = MT.void[G]

object VisualizerAdaptor:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](graphs: Graphs[F]): Resource[F, VisualizerAdaptor[F]] =
    Resource.eval(new VisualizerAdaptor(graphs).pure)

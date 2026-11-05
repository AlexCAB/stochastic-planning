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
| created: 2026-10-31 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization.graphs

import cats.effect.Async
import cats.effect.std.{AtomicCell, Dispatcher}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import planning.engine.tools.mpi.vizedit.gui.visualization.Graph
import planning.engine.tools.mpi.vizedit.gui.visualization.graphs.MapGraph.State
import planning.engine.tools.mpi.vizedit.gui.visualization.layouts.NodesView
import scalafx.Includes.*
import scalafx.scene.Node

class MapGraph[F[_]: {Async, Dispatcher, LoggerFactory}](
    graph: NetworkGraph[F],
    state: AtomicCell[F, State],
) extends Graph[F]:
  private val logger = LoggerFactory[F].getLogger

  // The whole map is rendered inside `evalUpdate`, so concurrent updates can't replace a view with an older one.
  private def update(change: State => State): F[Unit] = state.evalUpdate: st =>
    val newState = change(st)
    graph.render(newState.view).as(newState)

  override val node: Node = graph

  override def init: F[Unit] = graph.initGraph

  def nodesAdded(nodes: Map[MnId, NodeData]): F[Unit] =
    for
      _ <- update(st => st.copy(nodes = st.nodes ++ nodes))
      _ <- logger.info(s"MapGraph added ${nodes.size} nodes")
    yield ()

  def edgesAdded(keys: Set[MeKey]): F[Unit] =
    for
      _ <- update(st => st.copy(edges = st.edges ++ keys))
      _ <- logger.info(s"MapGraph added ${keys.size} edges")
    yield ()

  override def clear: F[Unit] =
    for
      _ <- state.evalUpdate(_ => graph.clear.as(State.empty))
      _ <- logger.info("MapGraph cleared")
    yield ()

object MapGraph:
  final case class State(nodes: Map[MnId, NodeData], edges: Set[MeKey]):
    // Visualizer updates are run independently, so edges can be added before their nodes, and are shown after them.
    def view: NetworkGraph.View = NodesView(nodes, edges.filter(_.mnIds.forall(nodes.contains)))

  object State:
    val empty: State = State(Map.empty, Set.empty)

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](): F[MapGraph[F]] =
    for
      graph <- NetworkGraph[F]()
      _ <- fx(graph.getStyleClass.add("map-graph"))
      state <- AtomicCell[F].of(State.empty)
    yield new MapGraph(graph, state)

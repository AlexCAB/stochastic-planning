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
| created: 2026-11-03 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization.graphs

import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.implicits.toFlatMapOps
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoTime
import planning.engine.planner.mpi.model.data.visualization.ActiveForest
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import planning.engine.tools.mpi.vizedit.gui.visualization.Graph
import planning.engine.tools.mpi.vizedit.gui.visualization.graphs.PlanningGraphs.View
import planning.engine.tools.mpi.vizedit.gui.visualization.layouts.NodesView
import scalafx.scene.layout.StackPane

// All planning graphs are stacked in one box, only the graph of the selected view is visible.
// Hidden graphs stay managed, so the box lays them out with the same size as the visible one.
// Must be constructed on the FX thread (see `PlanningGraphs.apply`).
class PlanningGraphs[F[_]: {Async, Dispatcher, LoggerFactory}](
    context: NetworkGraph[F],
    active: NetworkGraph[F],
    plan: NetworkGraph[F],
    actions: NetworkGraph[F],
) extends Graph[F]:
  private val logger = LoggerFactory[F].getLogger

  private val graphs: Map[View, NetworkGraph[F]] = Map(
    View.Context -> context,
    View.ActiveForest -> active,
    View.PlanningForest -> plan,
    View.ActionGraph -> actions,
  )

  private def setVisible(selected: View): Unit = graphs.foreach((view, graph) => graph.setVisible(view == selected))

  override val node: StackPane = new StackPane:
    delegate.getChildren.addAll(View.values.toSeq.map(view => graphs(view): javafx.scene.Node)*)

  setVisible(View.initial)

  // Must be called after the window containing the box was shown.
  override def init: F[Unit] = View.values.toList.traverse_(view => graphs(view).initGraph)

  // Rendered with the same layout and styles as the sample graph.
  def activeSteps(time: IoTime, forest: ActiveForest): F[Unit] =
    for
      edgeMnIds <- forest.edges.flatMap(_.mnIds).pure
      _ <- forest.nodes.keySet.assertContainsAllOf(edgeMnIds, s"Active forest at $time has edges with unknown nodes")
      _ <- forest.nodes.keySet.assertContainsAllOf(forest.roots, s"Active forest at $time has unknown roots")
      _ <- active.render(NodesView(forest.nodes.view.mapValues(_.data).toMap, forest.edges))
      _ <- logger.info(s"PlanningGraphs rendered active forest at $time, nodes: ${forest.nodes.size}")
    yield ()

  def show(view: View): F[Unit] =
    for
      _ <- fx(setVisible(view))
      _ <- logger.info(s"PlanningGraphs shown ${view.label}")
    yield ()

  override def clear: F[Unit] =
    for
      _ <- context.clear
      _ <- active.clear
      _ <- plan.clear
      _ <- actions.clear
      _ <- logger.info("PlanningGraphs cleared")
    yield ()

object PlanningGraphs:
  enum View(val label: String):
    case Context extends View("Context")
    case ActiveForest extends View("Active forest")
    case PlanningForest extends View("Planning forest")
    case ActionGraph extends View("Action graph")

  object View:
    val initial: View = View.Context

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](): F[PlanningGraphs[F]] =
    for
      context <- NetworkGraph[F]()
      active <- NetworkGraph[F]()
      _ <- fx(active.getStyleClass.add("active-graph"))
      plan <- NetworkGraph[F]()
      actions <- NetworkGraph[F]()
      planningGraphs <- fx(new PlanningGraphs(context, active, plan, actions))
    yield planningGraphs

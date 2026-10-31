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

package planning.engine.tools.mpi.vizedit.gui.components

import cats.effect.std.Dispatcher
import cats.effect.{Async, Ref}
import cats.syntax.all.*
import com.brunomnsilva.smartgraph.graph.{Digraph, DigraphEdgeList}
import com.brunomnsilva.smartgraph.graphview.*
import javafx.scene.shape.{ClosePath, MoveTo, Path}
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*

import java.net.URI
import scala.jdk.CollectionConverters.*

// Graph network view (SmartGraph panel) together with its graph model.
// Must be constructed on the FX thread (see `NetworkGraph.apply`).
class NetworkGraph[F[_]: {Async, Dispatcher}] private (
    graph: Digraph[String, String],
    initialized: Ref[F, Boolean], // SmartGraphPanel.init() must be called once, after the panel was shown.
    view: Ref[F, NetworkGraph.View], // Kept, since the panel can't display or style anything until it is initialized.
) extends SmartGraphPanel[String, String](
      graph,
      NetworkGraph.loadProperties(),
      new SmartCircularSortedPlacementStrategy(),
      NetworkGraph.cssUri,
    ):

  // Grid is centered in the panel, with steps reduced if needed to fit it (layer 0 is at the bottom).
  private def placeVertices(vertices: List[NetworkGraph.Vertex]): Unit =
    val panelVertices = graph.vertices().asScala.map(v => v.element -> v).toMap
    val maxColumn = vertices.map(_.position.column).maxOption.getOrElse(0.0)
    val maxLayer = vertices.map(_.position.layer.toDouble).maxOption.getOrElse(0.0)
    val columnStep = NetworkGraph.columnStep.min((getWidth - 2 * NetworkGraph.margin) / maxColumn.max(1.0))
    val layerStep = NetworkGraph.layerStep.min((getHeight - 2 * NetworkGraph.margin) / maxLayer.max(1.0))

    vertices.foreach: v =>
      val x = getWidth / 2 + (v.position.column - maxColumn / 2) * columnStep
      val y = getHeight / 2 + (maxLayer / 2 - v.position.layer) * layerStep
      setVertexPosition(panelVertices(v.element), x, y)

  // SmartArrow is drawn as two separate lines, which enclose no area, so it must be made a closed triangle to be filled.
  private def fillArrow(arrow: SmartStylableNode, styleClass: String): Unit = arrow match
    case path: Path =>
      if path.getElements.get(2).isInstanceOf[MoveTo] then
        path.getElements.remove(2)
        path.getElements.add(new ClosePath())
      arrow.addStyleClass(styleClass)
    case _ => ()

  private def styleEdges(edges: List[NetworkGraph.Edge]): Unit =
    val smartEdges = getSmartEdges.asScala.map(e => e.getUnderlyingEdge.element -> e).toMap

    edges.foreach: e =>
      val smartEdge = smartEdges(e.element)
      smartEdge.setStyleClass(e.styleClass)
      e.filledArrowClass.foreach(fillArrow(smartEdge.getStylableArrow, _))

  // Must be called on the FX thread, after the panel was initialized. Vertices can be placed and styled
  // only when already created by the panel, so `updateAndWait` must complete first.
  private def displayView: F[Unit] =
    for
      current <- view.get
      _ <- Async[F].delay:
        updateAndWait()
        placeVertices(current.vertices)
        current.vertices.foreach(v => getStylableVertex(v.element).setStyleClass(v.styleClass))
        styleEdges(current.edges)
    yield ()

  // Steps inside `onFx` run on the single FX thread without interleaving,
  // so the `initialized` check and the panel call it guards are atomic.
  // Must be called after the window containing this panel was shown.
  def initGraph: F[Unit] = onFx:
    for
      isInitialized <- initialized.getAndSet(true)
      _ <- (Async[F].delay(init()) *> displayView).unlessA(isInitialized)
    yield ()

  // Replaces the displayed graph. If the panel isn't initialized yet, it will be displayed by `initGraph`.
  def render(newView: NetworkGraph.View): F[Unit] = onFx:
    for
      _ <- view.set(newView)
      _ <- Async[F].delay:
        graph.vertices().asScala.toList.foreach(graph.removeVertex)
        newView.vertices.foreach(v => graph.insertVertex(v.element))
        newView.edges.foreach(e => graph.insertEdge(e.src, e.trg, e.element))
      isInitialized <- initialized.get
      _ <- displayView.whenA(isInitialized)
    yield ()

  def clear: F[Unit] = render(NetworkGraph.View.empty)

object NetworkGraph:
  // Position on the grid, converted to the panel coordinates when displayed.
  final case class Position(column: Double, layer: Int)

  // Elements identify vertices and edges, so must be unique. Vertex element is also displayed as its label.
  final case class Vertex(element: String, styleClass: String, position: Position)
  final case class Edge(
      element: String,
      src: String,
      trg: String,
      styleClass: String,
      filledArrowClass: Option[String] = None,
  )

  final case class View(vertices: List[Vertex], edges: List[Edge])

  object View:
    val empty: View = View(List.empty, List.empty)

  // Maximum grid steps and the panel margin, in pixels.
  private val columnStep: Double = 150
  private val layerStep: Double = 110
  private val margin: Double = 50

  private val PropertiesResource = "/smartgraph.properties"
  private val CssResource = "/smartgraph.css"

  def apply[F[_]: {Async, Dispatcher}](): F[NetworkGraph[F]] =
    for
      initialized <- Ref[F].of(false)
      view <- Ref[F].of(View.empty)
      networkGraph <- fx(new NetworkGraph(new DigraphEdgeList[String, String](), initialized, view))
    yield networkGraph

  private def loadProperties(): SmartGraphProperties =
    val stream = getClass.getResourceAsStream(PropertiesResource)
    try new SmartGraphProperties(stream)
    finally if stream != null then stream.close()

  private def cssUri: URI = getClass.getResource(CssResource).toURI

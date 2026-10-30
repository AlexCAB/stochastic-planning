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

  // Must be called on the FX thread, after the panel was initialized. Styles can be set only on nodes
  // already created by the panel, so `updateAndWait` must complete first. Automatic layout spreads the new
  // vertices, which the panel places randomly.
  private def displayView: F[Unit] =
    for
      current <- view.get
      _ <- Async[F].delay:
        updateAndWait()
        current.vertices.foreach(v => getStylableVertex(v.element).setStyleClass(v.styleClass))
        current.edges.foreach(e => getStylableEdge(e.element).setStyleClass(e.styleClass))
        setAutomaticLayout(current.vertices.nonEmpty)
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
  // Elements identify vertices and edges, so must be unique. Vertex element is also displayed as its label.
  final case class Vertex(element: String, styleClass: String)
  final case class Edge(element: String, src: String, trg: String, styleClass: String)

  final case class View(vertices: List[Vertex], edges: List[Edge])

  object View:
    val empty: View = View(List.empty, List.empty)

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

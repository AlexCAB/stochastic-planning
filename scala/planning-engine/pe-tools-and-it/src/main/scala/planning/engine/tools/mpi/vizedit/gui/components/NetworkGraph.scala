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
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphPanel, SmartGraphProperties}
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*

import java.net.URI
import scala.jdk.CollectionConverters.*

// Graph network view (SmartGraph panel) together with its graph model.
// Must be constructed on the FX thread (see `NetworkGraph.apply`).
class NetworkGraph[F[_]: {Async, Dispatcher}] private (
    graph: Digraph[String, String],
    initialized: Ref[F, Boolean], // SmartGraphPanel.init() must be called once, after the panel was shown.
) extends SmartGraphPanel[String, String](
      graph,
      NetworkGraph.loadProperties(),
      new SmartCircularSortedPlacementStrategy(),
      NetworkGraph.cssUri,
    ):

  // Steps inside `onFx` run on the single FX thread without interleaving,
  // so the `initialized` check and the panel call it guards are atomic.
  // Must be called after the window containing this panel was shown.
  def initGraph: F[Unit] = onFx:
    for
      isInitialized <- initialized.getAndSet(true)
      _ <- Async[F].delay(init()).unlessA(isInitialized)
    yield ()

  def clear: F[Unit] = onFx:
    for
      _ <- Async[F].delay(graph.vertices().asScala.toList.foreach(graph.removeVertex))
      isInitialized <- initialized.get
      _ <- Async[F].delay(update()).whenA(isInitialized)
    yield ()

object NetworkGraph:
  private val PropertiesResource = "/smartgraph.properties"
  private val CssResource = "/smartgraph.css"

  def apply[F[_]: {Async, Dispatcher}](): F[NetworkGraph[F]] =
    for
      initialized <- Ref[F].of(false)
      networkGraph <- fx(new NetworkGraph(new DigraphEdgeList[String, String](), initialized))
    yield networkGraph

  private def loadProperties(): SmartGraphProperties =
    val stream = getClass.getResourceAsStream(PropertiesResource)
    try new SmartGraphProperties(stream)
    finally if stream != null then stream.close()

  private def cssUri: URI = getClass.getResource(CssResource).toURI

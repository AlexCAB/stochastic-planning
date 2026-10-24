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
| created: 2026-10-24 |||||||||||*/

package planning.engine.yed

import cats.MonadThrow
import cats.effect.Sync
import cats.syntax.all.*
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory
import org.w3c.dom.{Document, Element, NodeList, Text}
import planning.engine.api.model.map.payload.*
import planning.engine.api.model.map.{MapAddSamplesRequest, MapInitRequest}
import planning.engine.common.enums.EdgeType
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.db.DbName
import planning.engine.common.values.node.HnName

import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import scala.annotation.tailrec
import scala.collection.immutable.ListMap

// yEd graph (*.graphml) of a map: the graph node with input/output nodes define the map,
// and each sample node with abstract/concrete nodes in its weakly connected component defines a sample.
final class Yed[F[_]: {MonadThrow, LoggerFactory}] private (
    nodes: List[ParsedNode], // In order of definition in the graph file
    edges: List[ParsedEdge],
    components: List[Set[String]], // Weakly connected components, as sets of node IDs
):
  private val logger = LoggerFactory[F].getLogger
  private val nodesById: Map[String, ParsedNode] = nodes.map(n => n.id -> n).toMap

  private def componentOf(nodeId: String): F[Set[String]] = components
    .find(_.contains(nodeId))
    .liftTo[F](AssertionError(s"Node $nodeId should be in one of the components"))

  private def nodesIn(component: Set[String]): List[ParsedNode] = nodes.filter(n => component.contains(n.id))

  def buildMapDefinition(dbName: DbName): F[MapInitRequest] =
    for
      graphNode <- nodes
        .collectFirst { case n: ParsedNode.Graph => n }
        .liftTo[F](AssertionError("Graph node should be defined"))
      component <- componentOf(graphNode.id)
      inputNodes = nodesIn(component).collect { case n: ParsedNode.Input => n.ioDef }
      _ <- inputNodes.assertNonEmpty("At least one input node should be defined")
      outputNodes = nodesIn(component).collect { case n: ParsedNode.Output => n.ioDef }
      _ <- outputNodes.assertNonEmpty("At least one output node should be defined")
      definition = MapInitRequest(dbName, Some(graphNode.name), graphNode.description, inputNodes, outputNodes)
      _ <- logger.info(s"Built map definition from yEd graph: $definition")
    yield definition

  private def toHiddenNodeDef(node: ParsedNode): Option[HiddenNodeDef] = node match
    case n: ParsedNode.Abstract => Some(AbstractNodeDef(n.name, n.description))
    case n: ParsedNode.Concrete => Some(ConcreteNodeDef(n.name, n.description, n.ioName, Json.fromString(n.value)))
    case _                      => None

  // Same hidden node can be drawn several times (in different samples), so nodes with the same name
  // must have the same parameters; the first defined description is used.
  private def addHiddenNode(
      acc: ListMap[HnName, HiddenNodeDef],
      node: HiddenNodeDef,
  ): F[ListMap[HnName, HiddenNodeDef]] = acc.get(node.name) match
    case None => acc.updated(node.name, node).pure
    case Some(existing) =>
      val (isSame, merged) = (existing, node) match
        case (e: AbstractNodeDef, n: AbstractNodeDef) => (true, e.copy(description = e.description.orElse(n.description)))
        case (e: ConcreteNodeDef, n: ConcreteNodeDef) =>
          (e.ioNodeName == n.ioNodeName && e.value == n.value, e.copy(description = e.description.orElse(n.description)))
        case _ => (false, existing)

      isSame
        .assertTrue(s"Node ${node.name.value} already exists with different params: $existing != $node")
        .as(acc.updated(node.name, merged))

  private def hnNameOf(node: ParsedNode): F[(String, HnName)] = node match
    case n: ParsedNode.Abstract => (n.id -> n.name).pure
    case n: ParsedNode.Concrete => (n.id -> n.name).pure
    case n                      => s"All nodes in the sample should be abstract or concrete nodes, found: $n".assertionError

  private def edgeTypeOf(edge: ParsedEdge): F[EdgeType] = edge.edgeType match
    case ParsedEdgeType.Link    => EdgeType.LINK.pure
    case ParsedEdgeType.Then    => EdgeType.THEN.pure
    case ParsedEdgeType.Support => s"All edges in the sample should be link or then edges, found: $edge".assertionError

  private def buildSample(sampleNode: ParsedNode.Sample): F[NewSampleData] =
    def toSampleEdge(names: Map[String, HnName])(edge: ParsedEdge): F[NewSampleEdge] =
      edgeTypeOf(edge).map(edgeType => NewSampleEdge(names(edge.sourceId), names(edge.targetId), edgeType))

    for
      component <- componentOf(sampleNode.id)
      sampleNodes = nodesById.filter((id, _) => component.contains(id) && id != sampleNode.id)
      _ <- (sampleNodes.keySet + sampleNode.id).assertSameElems(component, "Not all sample nodes found")
      sampleEdges = edges.filter: e =>
        (sampleNodes.contains(e.sourceId) || sampleNodes.contains(e.targetId)) && !e.isSupport
      _ <- sampleEdges
        .filterNot(e => sampleNodes.contains(e.sourceId) && sampleNodes.contains(e.targetId))
        .assertEmpty("Edges with end not in component found")
      names <- sampleNodes.values.toList.traverse(hnNameOf).map(_.toMap)
      sampleEdges <- sampleEdges.traverse(toSampleEdge(names))
    yield NewSampleData(
      probabilityCount = sampleNode.probabilityCount,
      utility = sampleNode.utility,
      name = Some(sampleNode.name),
      description = sampleNode.description,
      edges = sampleEdges,
    )

  def buildSamples: F[MapAddSamplesRequest] =
    for
      hiddenNodes <- nodes.flatMap(toHiddenNodeDef).foldLeftM(ListMap.empty[HnName, HiddenNodeDef])(addHiddenNode)
      sampleNodes = nodes.collect { case n: ParsedNode.Sample => n }
      _ <- sampleNodes.assertNonEmpty("At least one sample node should be defined")
      samples <- sampleNodes.traverse(buildSample)
      request = MapAddSamplesRequest(samples, hiddenNodes.values.toList)
      _ <- Validation.validateList[F](samples)
      _ <- Validation.validate[F](request)
      _ <- logger.info(s"Built samples from yEd graph: $request")
    yield request

object Yed:
  private val graphmlNs = "http://graphml.graphdrawing.org/xmlns"
  private val yNs = "http://www.yworks.com/xml/graphml"

  private def readXml(path: Path): Document =
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) // Prevents XXE
    factory.newDocumentBuilder().parse(path.toFile)

  private def elements(list: NodeList): List[Element] =
    (0 until list.getLength).toList.map(list.item).collect { case e: Element => e }

  // Label is the text of the first `y:NodeLabel` in the node's own `data` (not in nested graphs of group nodes),
  // without nested elements (label model, etc).
  private def parseNode[F[_]: MonadThrow](node: Element): F[ParsedNode] =
    val id = node.getAttribute("id")

    val label = elements(node.getChildNodes)
      .filter(e => e.getNamespaceURI == graphmlNs && e.getLocalName == "data")
      .flatMap(data => elements(data.getElementsByTagNameNS(yNs, "NodeLabel")))
      .headOption
      .map: nodeLabel =>
        val children = nodeLabel.getChildNodes
        (0 until children.getLength).map(children.item).takeWhile(_.isInstanceOf[Text]).map(_.getTextContent).mkString

    label match
      case Some(text) => ParsedNode.fromLabel[F](id, text)
      case None       => s"Node $id has no label".assertionError

  private def parseEdge[F[_]: MonadThrow](edge: Element): F[ParsedEdge] =
    val source = edge.getAttribute("source")
    val target = edge.getAttribute("target")

    elements(edge.getElementsByTagNameNS(yNs, "LineStyle")).headOption match
      case Some(lineStyle) => ParsedEdge.fromColor[F](source, target, lineStyle.getAttribute("color"))
      case None            => s"Edge $source -> $target has no line style".assertionError

  // Components of the graph with all edges treated as undirected, in order of their first node definition.
  private def weaklyConnectedComponents(nodeIds: List[String], edges: List[ParsedEdge]): List[Set[String]] =
    val neighbours: Map[String, List[String]] = edges
      .flatMap(e => List(e.sourceId -> e.targetId, e.targetId -> e.sourceId))
      .groupMap(_._1)(_._2)

    @tailrec def collect(frontier: List[String], visited: Set[String]): Set[String] = frontier match
      case Nil => visited
      case id :: rest =>
        val next = neighbours.getOrElse(id, Nil).filterNot(visited.contains)
        collect(next ++ rest, visited ++ next)

    nodeIds
      .foldLeft((List.empty[Set[String]], Set.empty[String])): (acc, id) =>
        val (found, visited) = acc
        if visited.contains(id) then acc
        else
          val component = collect(List(id), Set(id))
          (component :: found, visited ++ component)
      ._1.reverse

  def load[F[_]: {Sync, LoggerFactory}](yedPath: Path): F[Yed[F]] =
    val logger = LoggerFactory[F].getLogger

    for
      workDir <- Sync[F].delay(System.getProperty("user.dir"))
      _ <- logger.info(s"Loading yEd graph from $yedPath, work dir = $workDir")
      document <- Sync[F].blocking(readXml(yedPath))
      nodes <- elements(document.getElementsByTagNameNS(graphmlNs, "node")).traverse(parseNode[F])
      _ <- nodes.map(_.id).assertDistinct("Node IDs should be unique")
      edges <- elements(document.getElementsByTagNameNS(graphmlNs, "edge")).traverse(parseEdge[F])
      _ <- nodes.map(_.id).assertContainsAllOf(edges.flatMap(e => List(e.sourceId, e.targetId)), "Unknown edge ends")
      components = weaklyConnectedComponents(nodes.map(_.id), edges)
      _ <- logger.info(s"Parsed ${nodes.size} nodes from yEd graph, at $yedPath")
      _ <- logger.info(s"Parsed ${edges.size} edges from yEd graph, at $yedPath")
      _ <- logger.info(s"Found ${components.size} weakly connected components in yEd graph, at $yedPath")
    yield new Yed[F](nodes, edges, components)

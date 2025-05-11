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
| created: 2025-03-11 |||||||||||*/

package planning.engine.map.database

import cats.effect.Async
import cats.effect.Resource
import neotypes.{AsyncDriver, GraphDatabase, TransactionConfig}
import neotypes.model.types.Node
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.knowledge.graph.Metadata
import planning.engine.map.samples.SamplesState
import neotypes.cats.effect.implicits.*
import cats.syntax.all.*

/** Neo4jDatabase is a class that provides a high-level API to interact with a Neo4j database. It is responsible for
  * reading and writing data to the database.
  *
  * This class is a wrapper for the Neo4j database. It is used to connect to the database, clear it, insert root node,
  * etc. Knowledge graph have two parts:
  *   - root graph - have ROOT node, which contains the metadata of the graph and the input and output nodes
  *     definitions. Set of SAMPLE connected to the ROOT node, which contains the sample data.
  *   - hidden graph - contains the hidden nodes and edges, which are used to represent the knowledge map.
  */

trait Neo4jDatabaseLike[F[_]]:
  def initDatabase(
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]],
      samplesState: SamplesState
  ): F[Vector[Node]]

  def loadRootNodes: F[(Metadata, Vector[InputNode[F]], Vector[OutputNode[F]], SamplesState)]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String) extends Neo4jDatabaseLike[F] with Neo4jQueries:
  private val conf: TransactionConfig = TransactionConfig.default.withDatabase(dbName)

  private def splitIoNodes(ioNodes: Vector[IoNode[F]]): (Vector[InputNode[F]], Vector[OutputNode[F]]) =
    ioNodes.foldRight((Vector[InputNode[F]](), Vector[OutputNode[F]]())):
      case (inNode: InputNode[F], (inNodes, outNodes))   => (inNodes :+ inNode, outNodes)
      case (outNode: OutputNode[F], (inNodes, outNodes)) => (inNodes, outNodes :+ outNode)
      case (node, _)                                     => throw AssertionError(s"Invalid IoNode type: $node")

  override def initDatabase(
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]],
      samplesState: SamplesState
  ): F[Vector[Node]] = driver.transact(conf): tx =>
    for
      mtParams <- metadata.toQueryParams
      inParams <- inNodes.map(_.toQueryParams).sequence
      outParams <- outNodes.map(_.toQueryParams).sequence
      sampleParams <- samplesState.toQueryParams
      _ <- removeAllNodes(tx)
      staticNodes <- createStaticNodes(mtParams, sampleParams)(tx)
      ioNodes <- (inParams ++ outParams).map(params => createIoNode(params)(tx)).sequence
    yield staticNodes ++ ioNodes

  override def loadRootNodes: F[(Metadata, Vector[InputNode[F]], Vector[OutputNode[F]], SamplesState)] = driver
    .transact(conf): tx =>
      for
        Vector(rootNode, samplesNode) <- readStaticNodes(tx)
        metadata <- Metadata.fromNode(rootNode)
        samplesState <- SamplesState.fromNode(samplesNode)
        rawIoNodes <- readIoNodes(tx)
        ioNodes <- rawIoNodes.map(n => IoNode.fromNode(n)).sequence
        (inNodes, outNodes) = splitIoNodes(ioNodes)
      yield (metadata, inNodes, outNodes, samplesState)

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConnectionConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)

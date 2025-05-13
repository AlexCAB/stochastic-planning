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
| created: 2025-05-01 |||||||||||*/

package planning.engine.map.database

import cats.effect.Async
import neotypes.AsyncTransaction
import neotypes.mappers.ResultMapper
import neotypes.model.query.QueryParam
import neotypes.model.types.Node
import neotypes.query.QueryArg.Param
import neotypes.syntax.all.*
import planning.engine.common.properties.PROP_NAME
import planning.engine.map.database.Neo4jQueries.*

trait Neo4jQueries:

  extension (params: Map[String, Param])
    def qp: Map[String, QueryParam] = params.view.mapValues(_.param).toMap

  def removeAllNodes[F[_]: Async](tx: AsyncTransaction[F]): F[Unit] = c"MATCH (n) DETACH DELETE n".execute.void(tx)

  def createStaticNodes[F[_]: Async](rootPrams: Map[String, Param], samplesParams: Map[String, Param])(
      tx: AsyncTransaction[F]
  ): F[Vector[Node]] =
    c"""
      CREATE (root:#${ROOT_LABEL} ${rootPrams.qp}),
             (io:#${IO_NODES_LABEL}),
             (samples: #${SAMPLES_LABEL} ${samplesParams.qp}),
             (root)-[:IO_NODES_EDGE]->(io),
             (root)-[:SAMPLES_EDGE]->(samples)
      RETURN [root, io, samples]
      """.query(ResultMapper.vector(ResultMapper.node)).single(tx)

  def createIoNode[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      MATCH (io_root:#${IO_NODES_LABEL})
      CREATE (io_node: #${IO_NODE_LABEL} ${props.qp}),
             (io_root)-[:IO_NODE_EDGE]->(io_node)
      RETURN io_node
      """.query(ResultMapper.node).single(tx)

  def readStaticNodes[F[_]: Async](tx: AsyncTransaction[F]): F[Vector[Node]] =
    c"""
      MATCH (root:#${ROOT_LABEL})-->(samples: SAMPLES)
      RETURN [root, samples]
      """.query(ResultMapper.vector(ResultMapper.node)).single(tx)

  def readIoNodes[F[_]: Async](tx: AsyncTransaction[F]): F[Vector[Node]] =
    c"""
      MATCH (:#${IO_NODES_LABEL})-->(io_nodes:#${IO_NODE_LABEL})
      RETURN io_nodes
      """.query(ResultMapper.node).vector(tx)

  def addConcreteNode[F[_]: Async](
      ioNodeName: String,
      props: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (io:#${IO_NODE_LABEL} {#${PROP_NAME.NAME}: $ioNodeName})
      CREATE (concrete: #${CONCRETE_LABEL} ${props.qp}),
             (concrete)-[:IO_VALUE_EDGE]->(io)
      RETURN [io, concrete]
      """.query(ResultMapper.list(ResultMapper.node)).single(tx)

  def addAbstractNode[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      CREATE (abstract: #${ABSTRACT_LABEL} ${props.qp})
      RETURN abstract
      """.query(ResultMapper.node).single(tx)

object Neo4jQueries:
  val ROOT_LABEL = "ROOT"
  val SAMPLES_LABEL = "SAMPLES"
  val IO_NODES_LABEL = "IO_NODES"
  val IO_NODE_LABEL = "IO_NODE"
  val CONCRETE_LABEL = "CONCRETE"
  val ABSTRACT_LABEL = "ABSTRACT"

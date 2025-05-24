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
import planning.engine.common.values.db.Label
import planning.engine.map.database.Neo4jQueries.*

trait Neo4jQueries:

  extension (params: Map[String, Param])
    private def qp: Map[String, QueryParam] = params.view.mapValues(_.param).toMap

  def removeAllNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Unit] = c"MATCH (n) DETACH DELETE n".execute.void(tx)

  def createStaticNodesQuery[F[_]: Async](rootPrams: Map[String, Param])(
      tx: AsyncTransaction[F]
  ): F[List[Node]] =
    c"""
      CREATE (root: #${ROOT_LABEL.s} ${rootPrams.qp}),
             (io: #${IO_NODES_LABEL.s}),
             (samples: #${SAMPLES_LABEL.s}),
             (root)-[:IO_NODES_EDGE]->(io),
             (root)-[:SAMPLES_EDGE]->(samples)
      RETURN [root, io, samples]
      """.query(ResultMapper.list(ResultMapper.node)).single(tx)

  def readStaticNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (root: #${ROOT_LABEL.s})-->(samples: SAMPLES)
      RETURN [root, samples]
      """.query(ResultMapper.list(ResultMapper.node)).single(tx)

  def createIoNodeQuery[F[_]: Async](label: Label, props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      MATCH (io_root: #${IO_NODES_LABEL.s})
      CREATE (io_node: #${IO_LABEL.s}: #${label.s} ${props.qp}),
             (io_root)-[:IO_NODE_EDGE]->(io_node)
      RETURN io_node
      """.query(ResultMapper.node).single(tx)

  def readIoNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (: #${IO_NODES_LABEL.s})-->(io_nodes:#${IO_LABEL.s})
      RETURN io_nodes
      """.query(ResultMapper.node).list(tx)

    

  def findNextHnIdIdQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] = ???

  def findNextSampleIdQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] = ???

  def countSamplesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] = ???



  def addConcreteNodeQuery[F[_]: Async](
      ioNodeName: String,
      props: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (io: #${IO_LABEL.s} {#${PROP_NAME.NAME}: $ioNodeName})
      CREATE (concrete: #${HN_LABEL.s}: #${CONCRETE_LABEL.s} ${props.qp}),
             (concrete)-[:IO_VALUE_EDGE]->(io)
      RETURN [io, concrete]
      """.query(ResultMapper.list(ResultMapper.node)).single(tx)

  def addAbstractNodeQuery[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      CREATE (abstract: #${HN_LABEL.s}: #${ABSTRACT_LABEL.s} ${props.qp})
      RETURN abstract
      """.query(ResultMapper.node).single(tx)

  def findHiddenIdsNodesByNamesQuery[F[_]: Async](names: List[String])(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (n: #${HN_LABEL.s})
      WHERE n.#${PROP_NAME.NAME} IN $names
      RETURN n.#${PROP_NAME.HN_ID}
      """.query(ResultMapper.long).list(tx)

  def findAbstractNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (n: #${HN_LABEL.s}: #${ABSTRACT_LABEL.s}) 
      WHERE n.#${PROP_NAME.HN_ID} IN $ids
      RETURN n
      """.query(ResultMapper.node).list(tx)

  def findConcreteNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F]): F[List[List[Node]]] =
    c"""
      MATCH (cn: #${HN_LABEL.s}: #${CONCRETE_LABEL.s})->(io: #${IO_LABEL.s})
      WHERE cn.#${PROP_NAME.HN_ID} IN $ids
      RETURN [cn, io]
      """.query(ResultMapper.list(ResultMapper.node)).list(tx)

  def countAllHiddenNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (n: #${HN_LABEL.s}) RETURN count(n)
      """.query(ResultMapper.long).single(tx)

object Neo4jQueries:
  val ROOT_LABEL = Label("Root")
  val SAMPLES_LABEL = Label("Samples")
  val IO_NODES_LABEL = Label("IoNodes")
  val IO_LABEL = Label("Io")
  val IN_LABEL = Label("In")
  val OUT_LABEL = Label("Out")
  val HN_LABEL = Label("Hn")
  val CONCRETE_LABEL = Label("Concrete")
  val ABSTRACT_LABEL = Label("Abstract")

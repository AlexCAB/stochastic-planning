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
import neotypes.query.{DeferredQuery, ResultType}
import neotypes.query.QueryArg.Param
import neotypes.syntax.all.*
import planning.engine.common.properties.PROP
import planning.engine.common.values.db.Neo4j.*
import cats.syntax.all.*

trait Neo4jQueries:

  extension (params: Map[String, Param])
    private def qp: Map[String, QueryParam] = params.view.mapValues(_.param).toMap

  extension [T, RT <: ResultType](query: DeferredQuery[T, RT])
    private def singleNode[F[_]: Async](tx: AsyncTransaction[F]): F[query.RT.AsyncR[T]] = query
      .single(tx)
      .recoverWith(err => Async[F].raiseError(new RuntimeException(s"Failed to execute query: ${query.query}", err)))

  def removeAllNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Unit] = c"MATCH (n) DETACH DELETE n".execute.void(tx)

  def createStaticNodesQuery[F[_]: Async](rootPrams: Map[String, Param])(
      tx: AsyncTransaction[F]
  ): F[List[Node]] =
    c"""
      CREATE (root: #$ROOT_LABEL ${rootPrams.qp}),
             (io: #$IO_NODES_LABEL {name: "#$SAMPLES_LABEL"}),
             (samples: #$SAMPLES_LABEL {name: "#$SAMPLES_LABEL"}),
             (root)-[:IO_NODES_EDGE]->(io),
             (root)-[:SAMPLES_EDGE]->(samples)
      RETURN [root, io, samples]
      """.query(ResultMapper.list(ResultMapper.node)).singleNode(tx)

  def readStaticNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (root: #$ROOT_LABEL)-->(samples: #$SAMPLES_LABEL)
      RETURN [root, samples]
      """.query(ResultMapper.list(ResultMapper.node)).singleNode(tx)

  def createIoNodeQuery[F[_]: Async](label: Label, props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      MATCH (io_root: #$IO_NODES_LABEL)
      CREATE (io_node: #$IO_LABEL: #$label ${props.qp}),
             (io_root)-[:IO_NODE_EDGE]->(io_node)
      RETURN io_node
      """.query(ResultMapper.node).singleNode(tx)

  def readIoNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (: #$IO_NODES_LABEL)-->(io_nodes:#$IO_LABEL)
      RETURN io_nodes
      """.query(ResultMapper.node).list(tx)

  def getAndIncrementNextHnIdQuery[F[_]: Async](numOfIds: Long)(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (root: #$ROOT_LABEL)
      SET root.#${PROP.NEXT_HN_ID} = root.#${PROP.NEXT_HN_ID} + $numOfIds
      RETURN range((root.#${PROP.NEXT_HN_ID} - $numOfIds), root.#${PROP.NEXT_HN_ID} - 1, +1)
      """.query(ResultMapper.list(ResultMapper.long)).singleNode(tx)

  def countSamplesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (: #$SAMPLES_LABEL)-->(sn: #$SAMPLE_LABEL)
      RETURN count(sn)
      """.query(ResultMapper.long).singleNode(tx)

  def addConcreteNodeQuery[F[_]: Async](
      ioNodeName: String,
      props: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (io: #$IO_LABEL {#${PROP.NAME}: $ioNodeName})
      CREATE (concrete: #$HN_LABEL: #$CONCRETE_LABEL ${props.qp}),
             (concrete)-[:IO_VALUE_EDGE]->(io)
      RETURN [io, concrete]
      """.query(ResultMapper.list(ResultMapper.node)).singleNode(tx)

  def addAbstractNodeQuery[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F]): F[Node] =
    c"""
      CREATE (abstract: #$HN_LABEL: #$ABSTRACT_LABEL ${props.qp})
      RETURN abstract
      """.query(ResultMapper.node).singleNode(tx)

  def findHiddenIdsNodesByNamesQuery[F[_]: Async](names: List[String])(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (n: #$HN_LABEL)
      WHERE n.#${PROP.NAME} IN $names
      RETURN n.#${PROP.HN_ID}
      """.query(ResultMapper.long).list(tx)

  def findAbstractNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (n: #$HN_LABEL: #$ABSTRACT_LABEL) 
      WHERE n.#${PROP.HN_ID} IN $ids
      RETURN n
      """.query(ResultMapper.node).list(tx)

  def findConcreteNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F]): F[List[List[Node]]] =
    c"""
      MATCH (cn: #$HN_LABEL: #$CONCRETE_LABEL)-->(io: #$IO_LABEL)
      WHERE cn.#${PROP.HN_ID} IN $ids
      RETURN [cn, io]
      """.query(ResultMapper.list(ResultMapper.node)).list(tx)

  def countAllHiddenNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (n: #$HN_LABEL) RETURN count(n)
      """.query(ResultMapper.long).singleNode(tx)

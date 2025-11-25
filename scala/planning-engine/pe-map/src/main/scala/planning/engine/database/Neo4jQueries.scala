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

package planning.engine.database

import cats.effect.Async
import neotypes.AsyncTransaction
import neotypes.mappers.ResultMapper
import neotypes.model.query.QueryParam
import neotypes.model.types.{Node, Relationship}
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
    private def singleResult[F[_]: Async](tx: AsyncTransaction[F]): F[query.RT.AsyncR[T]] = query
      .single(tx)
      .recoverWith(err => Async[F].raiseError(new RuntimeException(s"Failed to execute query: ${query.query}", err)))

    private def listResult[F[_]: Async](tx: AsyncTransaction[F]): F[query.RT.AsyncR[List[T]]] = query
      .list(tx)
      .recoverWith(err => Async[F].raiseError(new RuntimeException(s"Failed to execute query: ${query.query}", err)))

  protected def checkConnectionQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"MATCH (n) RETURN count(n)".query(ResultMapper.long).singleResult(tx)

  protected def removeAllNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Unit] =
    c"MATCH (n) DETACH DELETE n".execute.void(tx)

  protected def createStaticNodesQuery[F[_]: Async](
      rootPrams: Map[String, Param],
      samplesParams: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      CREATE (root: #$ROOT_LABEL ${rootPrams.qp}),
             (io: #$IO_NODES_LABEL {name: "#$IO_NODES_LABEL"}),
             (samples: #$SAMPLES_LABEL ${samplesParams.qp}),
             (root)-[:IO_NODES_EDGE]->(io),
             (root)-[:SAMPLES_EDGE]->(samples)
      RETURN [root, io, samples]
      """.query(ResultMapper.list(ResultMapper.node)).singleResult(tx)

  protected def readStaticNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (root: #$ROOT_LABEL)-->(samples: #$SAMPLES_LABEL)
      RETURN [root, samples]
      """.query(ResultMapper.list(ResultMapper.node)).singleResult(tx)

  protected def createIoNodeQuery[F[_]: Async](
      label: Label,
      props: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[Node] =
    c"""
      MATCH (io_root: #$IO_NODES_LABEL)
      CREATE (io_node: #$IO_LABEL: #$label ${props.qp}),
             (io_root)-[:IO_NODE_EDGE]->(io_node)
      RETURN io_node
      """.query(ResultMapper.node).singleResult(tx)

  protected def readIoNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (: #$IO_NODES_LABEL)-->(io_nodes:#$IO_LABEL)
      RETURN io_nodes
      """.query(ResultMapper.node).list(tx)

  protected def getNextHnIdQuery[F[_]: Async](numOfIds: Long)(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (root: #$ROOT_LABEL)
      SET root.#${PROP.NEXT_HN_ID} = root.#${PROP.NEXT_HN_ID} + $numOfIds
      RETURN range((root.#${PROP.NEXT_HN_ID} - $numOfIds), root.#${PROP.NEXT_HN_ID} - 1, +1)
      """.query(ResultMapper.list(ResultMapper.long)).singleResult(tx)

  protected def countSamplesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (: #$SAMPLES_LABEL)-->(sn: #$SAMPLE_LABEL)
      RETURN count(sn)
      """.query(ResultMapper.long).singleResult(tx)

  protected def addConcreteNodeQuery[F[_]: Async](
      ioNodeName: String,
      props: Map[String, Param]
  )(tx: AsyncTransaction[F]): F[(Long, Option[String])] =
    c"""
      MATCH (io: #$IO_LABEL {#${PROP.NAME}: $ioNodeName})
      CREATE (concrete: #$HN_LABEL: #$CONCRETE_LABEL ${props.qp}),
             (concrete)-[:IO_VALUE_EDGE]->(io)
      RETURN [concrete.#${PROP.HN_ID}, concrete.#${PROP.NAME}]
      """.query(ResultMapper.tuple[Long, Option[String]]).singleResult(tx)

  protected def addAbstractNodeQuery[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F])
      : F[(Long, Option[String])] =
    c"""
      CREATE (abstract: #$HN_LABEL: #$ABSTRACT_LABEL ${props.qp})
      RETURN [abstract.#${PROP.HN_ID}, abstract.#${PROP.NAME}]
      """.query(ResultMapper.tuple[Long, Option[String]]).singleResult(tx)

  protected def findHiddenIdsNodesByNamesQuery[F[_]: Async](names: List[String])(tx: AsyncTransaction[F])
      : F[List[(String, Long)]] =
    c"""
      MATCH (n: #$HN_LABEL)
      WHERE n.#${PROP.NAME} IN $names
      RETURN [n.#${PROP.NAME}, n.#${PROP.HN_ID}]
      """.query(ResultMapper.tuple[String, Long]).list(tx)

  protected def findAbstractNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (n: #$HN_LABEL: #$ABSTRACT_LABEL) 
      WHERE n.#${PROP.HN_ID} IN $ids
      RETURN n
      """.query(ResultMapper.node).listResult(tx)

  protected def findConcreteNodesByIdsQuery[F[_]: Async](ids: List[Long])(tx: AsyncTransaction[F])
      : F[List[(Node, String)]] =
    c"""
      MATCH (cn: #$HN_LABEL: #$CONCRETE_LABEL)-->(io: #$IO_LABEL)
      WHERE cn.#${PROP.HN_ID} IN $ids
      RETURN [cn, io.#${PROP.NAME}]
      """.query(ResultMapper.tuple[Node, String]).listResult(tx)

  protected def countAllHiddenNodesQuery[F[_]: Async](tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (n: #$HN_LABEL) RETURN count(n)
      """.query(ResultMapper.long).singleResult(tx)

  protected def addHiddenEdge[F[_]: Async](
      sourceId: Long,
      targetId: Long,
      label: Label
  )(tx: AsyncTransaction[F]): F[Unit] =
    c"""
      MATCH (source: #$HN_LABEL {#${PROP.HN_ID}: $sourceId}), (target: #$HN_LABEL {#${PROP.HN_ID}: $targetId})
      MERGE (source)-[:#$label]->(target)
      """.execute.void(tx)

  protected def getNextSampleIdsQuery[F[_]: Async](numOfIds: Long)(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (samples: #$SAMPLES_LABEL)
      SET samples.#${PROP.NEXT_SAMPLES_ID} = samples.#${PROP.NEXT_SAMPLES_ID} + $numOfIds
      RETURN range((samples.#${PROP.NEXT_SAMPLES_ID} - $numOfIds), samples.#${PROP.NEXT_SAMPLES_ID} - 1, +1)
      """.query(ResultMapper.list(ResultMapper.long)).singleResult(tx)

  protected def getNextHnIndexQuery[F[_]: Async](hnId: Long, numOfIds: Long)(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (hn: #$HN_LABEL {#${PROP.HN_ID}: $hnId})
      SET hn.#${PROP.NEXT_HN_INDEX} = hn.#${PROP.NEXT_HN_INDEX} + $numOfIds
      RETURN range((hn.#${PROP.NEXT_HN_INDEX} - $numOfIds), hn.#${PROP.NEXT_HN_INDEX} - 1, +1)
      """.query(ResultMapper.list(ResultMapper.long)).singleResult(tx)

  protected def addSampleQuery[F[_]: Async](props: Map[String, Param])(tx: AsyncTransaction[F]): F[Long] =
    c"""
      MATCH (samples: #$SAMPLES_LABEL)   
      CREATE (sample: #$SAMPLE_LABEL ${props.qp}),
             (samples)-[:SAMPLE_EDGE]->(sample)
      RETURN sample.#${PROP.SAMPLE_ID}
      """.query(ResultMapper.long).singleResult(tx)

  protected def addSampleEdgeQuery[F[_]: Async](
      snId: Long,
      tnId: Long,
      label: Label,
      propName: String,
      propValue: List[Long]
  )(tx: AsyncTransaction[F]): F[String] =
    c"""
      MATCH (:#$HN_LABEL {#${PROP.HN_ID}: $snId})-[edge:#$label]->(:#$HN_LABEL {#${PROP.HN_ID}: $tnId})
      SET edge.#$propName = $propValue
      RETURN elementId(edge)
      """.query(ResultMapper.string).singleResult(tx)

  protected def updateNumberOfSamplesQuery[F[_]: Async](numOfSamples: Long)(tx: AsyncTransaction[F]): F[Unit] =
    c"""
      MATCH (samples: #$SAMPLES_LABEL)
      SET samples.#${PROP.SAMPLES_COUNT} = samples.#${PROP.SAMPLES_COUNT} + $numOfSamples
      """.execute.void(tx)

  protected def getNextEdgesQuery[F[_]: Async](curHdId: Long)(tx: AsyncTransaction[F]): F[List[(Relationship, Long)]] =
    c"""
      MATCH (:#$HN_LABEL {#${PROP.HN_ID}: $curHdId})-[edge]->(t:#$HN_LABEL) 
      WHERE edge:#$LINK_LABEL OR edge:#$THEN_LABEL
      RETURN [edge, t.#${PROP.HN_ID}]
      """.query(ResultMapper.tuple[Relationship, Long]).listResult(tx)

  protected def getSamplesQuery[F[_]: Async](sampleIds: List[Long])(tx: AsyncTransaction[F]): F[List[Node]] =
    c"""
      MATCH (: #$SAMPLES_LABEL)-->(sample: #$SAMPLE_LABEL)
      WHERE sample.#${PROP.SAMPLE_ID} IN $sampleIds
      RETURN sample
      """.query(ResultMapper.node).listResult(tx)

  protected def getSampleNamesQuery[F[_]: Async](sampleIds: List[Long])(tx: AsyncTransaction[F])
      : F[List[(Long, Option[String])]] =
    c"""
      MATCH (: #$SAMPLES_LABEL)-->(sample: #$SAMPLE_LABEL)
      WHERE sample.#${PROP.SAMPLE_ID} IN $sampleIds
      RETURN [sample.#${PROP.SAMPLE_ID}, sample.#${PROP.NAME}]
      """.query(ResultMapper.tuple[Long, Option[String]]).listResult(tx)

  protected def getSampleEdgesQuery[F[_]: Async](sampleIds: List[String])(tx: AsyncTransaction[F])
      : F[List[(Long, Relationship, Long)]] =
    c"""
      MATCH (source: #$HN_LABEL)-[edge]->(target: #$HN_LABEL)
      WHERE (edge:#$LINK_LABEL OR edge:#$THEN_LABEL) AND any(k IN keys(edge) WHERE k IN $sampleIds)
      RETURN [source.#${PROP.HN_ID}, edge, target.#${PROP.HN_ID}]
      """.query(ResultMapper.tuple[Long, Relationship, Long]).listResult(tx)

  protected def findHiddenNodesByIoValueQuery[F[_]: Async](ioNodeName: String, ioIndex: Long)(tx: AsyncTransaction[F])
      : F[List[Node]] =
    c"""
      MATCH (io: #$IO_LABEL {#${PROP.NAME}: $ioNodeName})<--(cn: #$CONCRETE_LABEL {#${PROP.IO_INDEX}: $ioIndex})
      RETURN cn
      """.query(ResultMapper.node).listResult(tx)

  protected def findParentIdsQuery[F[_]: Async](targetId: Long, label: Label)(tx: AsyncTransaction[F]): F[List[Long]] =
    c"""
      MATCH (:#$HN_LABEL {#${PROP.HN_ID}: $targetId})<-[edge:#$label]-(source:#$HN_LABEL)
      RETURN source.#${PROP.HN_ID}
      """.query(ResultMapper.long).listResult(tx)

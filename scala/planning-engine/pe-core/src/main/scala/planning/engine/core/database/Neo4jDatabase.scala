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

package planning.engine.core.database

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import neotypes.AsyncDriver
import neotypes.GraphDatabase
import neotypes.cats.effect.implicits.*
import neotypes.mappers.ResultMapper
import neotypes.model.types.Node
import neotypes.syntax.all.*
import planning.engine.common.config.Neo4jConnectionConf

/** Neo4jDatabase is a class that provides a high-level API to interact with a Neo4j database. It is responsible for
  * reading and writing data to the database.
  *
  * This class is a wrapper for the Neo4j database. It is used to connect to the database, clear it, insert root node,
  * etc. Knowledge graph have two parts:
  *   - root graph - have ROOT node, which contains the metadata of the graph and the input and output nodes
  *     definitions. Set of SAMPLE connected to the ROOT node, which contains the sample data.
  *   - hidden graph - contains the hidden nodes and edges, which are used to represent the knowledge map.
  */

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String):

  def readRootNode: F[Node] = c"MATCH (r: ROOT) RETURN r"
    .query(ResultMapper.node)
    .list(driver)
    .flatMap {
      case head :: Nil => Async[F].pure(head)
      case list        => Async[F].raiseError(new AssertionError(s"Expected exactly one ROOT node, but got: $list"))
    }

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConnectionConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)

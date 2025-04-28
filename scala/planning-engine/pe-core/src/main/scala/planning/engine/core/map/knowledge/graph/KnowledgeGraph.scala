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
| created: 2025-03-15 |||||||||||*/

package planning.engine.core.map.knowledge.graph

import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory
import planning.engine.core.database.Neo4jDatabase
import planning.engine.core.map.io.node.{InputNode, OutputNode}

trait KnowledgeGraphLke[F[_]]:
  def metadata: Metadata
  def inputNodes: Vector[InputNode[F]]
  def outputNodes: Vector[OutputNode[F]]
  def countHiddenNodes: F[Long]

class KnowledgeGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: Metadata,
    val inputNodes: Vector[InputNode[F]],
    val outputNodes: Vector[OutputNode[F]],
    database: Neo4jDatabase[F]
) extends KnowledgeGraphLke[F]:
  def countHiddenNodes: F[Long] = ???

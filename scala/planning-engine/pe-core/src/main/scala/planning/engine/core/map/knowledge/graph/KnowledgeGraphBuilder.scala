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
| created: 2025-04-25 |||||||||||*/

package planning.engine.core.map.knowledge.graph

import cats.effect.{Async, Resource, Sync}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.core.database.Neo4jDatabase
import planning.engine.core.map.io.node.{InputNode, OutputNode}

trait KnowledgeGraphBuilderLike[F[_]]:
  def init(metadata: Metadata, inNodes: Vector[InputNode[F]], outNodes: Vector[OutputNode[F]]): F[KnowledgeGraphLke[F]]
  def load: F[KnowledgeGraphLke[F]]

class KnowledgeGraphBuilder[F[_]: {Async, LoggerFactory}](database: Neo4jDatabase[F])
    extends KnowledgeGraphBuilderLike[F]:

  override def init(
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]]
  ): F[KnowledgeGraphLke[F]] = ???

  override def load: F[KnowledgeGraphLke[F]] = ???

object KnowledgeGraphBuilder:
  def apply[F[_]: {Async, LoggerFactory}](database: Neo4jDatabase[F]): Resource[F, KnowledgeGraphBuilder[F]] =
    Resource.eval(Sync[F].delay(new KnowledgeGraphBuilder[F](database)))

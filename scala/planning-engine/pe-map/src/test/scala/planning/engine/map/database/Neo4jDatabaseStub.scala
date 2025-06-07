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
| created: 2025-06-08 |||||||||||*/

package planning.engine.map.database

import cats.effect.IO
import neotypes.model.types.Node
import planning.engine.common.values.text.Name as HnId
import planning.engine.map.graph.MapMetadata
import planning.engine.map.io.node.{InputNode, OutputNode}

class Neo4jDatabaseStub extends Neo4jDatabaseLike[IO]:
  def initDatabase(metadata: MapMetadata, inNodes: List[InputNode[IO]], outNodes: List[OutputNode[IO]]): F[List[Node]]
  def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]], MapCacheState[F])]

  def createConcreteNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[ConcreteNode[F]]],
      updateCache: List[ConcreteNode[F]] => F[R]
  ): F[(R, List[Node], List[ConcreteNode[F]])]

  def createAbstractNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[AbstractNode[F]]],
      updateCache: List[AbstractNode[F]] => F[R]
  ): F[(R, List[Node], List[AbstractNode[F]])]

  def findHiddenNodesByNames[R <: MapCacheLike[F]](
      names: List[Name],
      loadCached: List[HnId] => F[(R, List[HiddenNode[F]])],
      getIoNode: Name => F[IoNode[F]],
      updateCache: (R, List[HiddenNode[F]]) => F[R]
  ): F[(R, List[HiddenNode[F]])]

  def countHiddenNodes: F[Long]

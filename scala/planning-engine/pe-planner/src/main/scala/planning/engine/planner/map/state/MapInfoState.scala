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
| created: 2025-12-24 |||||||||||*/

package planning.engine.planner.map.state

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoName
import planning.engine.common.errors.*
import planning.engine.map.data.MapMetadata
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}

final case class MapInfoState[F[_]: MonadThrow](
    metadata: MapMetadata,
    inNodes: Map[IoName, InputNode[F]],
    outNodes: Map[IoName, OutputNode[F]]
):
  lazy val allIoNodes: Map[IoName, IoNode[F]] = inNodes ++ outNodes
  lazy val isEmpty: Boolean = inNodes.isEmpty && outNodes.isEmpty

  def getIoNode(name: IoName): F[IoNode[F]] = allIoNodes.get(name) match
    case Some(node) => node.pure
    case _          => s"IO node with name $name not found".assertionError

  override lazy val toString: String =
    s"MapInfoState(name = ${metadata.name}, inNodes = ${inNodes.size}, outNodes = ${outNodes.size})"

object MapInfoState:
  def empty[F[_]: MonadThrow]: MapInfoState[F] = new MapInfoState[F](MapMetadata.empty, Map.empty, Map.empty)

  def apply[F[_]: MonadThrow](
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[MapInfoState[F]] =
    for
      _ <- (inNodes ++ outNodes).map(_.name).assertDistinct("All IO node names must be distinct")
      inNodesMap = inNodes.map(n => n.name -> n).toMap
      outNodesMap = outNodes.map(n => n.name -> n).toMap
    yield new MapInfoState[F](metadata, inNodesMap, outNodesMap)

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
| created: 2026-10-24 |||||||||||*/

package planning.engine.yed

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*

enum ParsedEdgeType:
  case Link, Then, Support

// Edge of the yEd graph, type is defined by the edge line color.
final case class ParsedEdge(sourceId: String, targetId: String, edgeType: ParsedEdgeType):
  def isLink: Boolean = edgeType == ParsedEdgeType.Link
  def isThen: Boolean = edgeType == ParsedEdgeType.Then
  def isSupport: Boolean = edgeType == ParsedEdgeType.Support

  override def toString: String = s"Edge($sourceId - $edgeType -> $targetId)"

object ParsedEdge:
  def fromColor[F[_]: MonadThrow](sourceId: String, targetId: String, color: String): F[ParsedEdge] =
    for
      _ <- sourceId.nonEmpty.assertTrue("Source id should be defined")
      _ <- targetId.nonEmpty.assertTrue("Target id should be defined")
      edgeType <- color.toUpperCase match
        case "#FF0000" => ParsedEdgeType.Then.pure
        case "#00FF00" => ParsedEdgeType.Link.pure
        case "#888888" => ParsedEdgeType.Support.pure
        case _         => s"Invalid color $color for edge $sourceId -> $targetId".assertionError
    yield ParsedEdge(sourceId, targetId, edgeType)

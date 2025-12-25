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

package planning.engine.api.service.map

import cats.effect.Async
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.errors.*

abstract class MapServiceBase[F[_]: {Async, LoggerFactory}]:
  private[map] def composeHnIdMap(
      foundHnIdMap: Map[HnName, Set[HnId]],
      newConHnIds: Map[HnId, Option[HnName]],
      newAbsHnIds: Map[HnId, Option[HnName]]
  ): F[Map[HnName, HnId]] =
    for
      foundIds <- foundHnIdMap.toList.traverse:
        case (name, ids) if ids.size == 1 => (name -> ids.head).pure
        case (name, ids) => s"Expect exactly one variable with name $name, but got: $ids".assertionError
      newIds <- (newConHnIds.toList ++ newAbsHnIds.toList).traverse:
        case (id, Some(name)) => (name -> id).pure
        case (id, _)          => s"No name found for hnId: $id".assertionError
      allHnNames = foundIds ++ newIds
      _ <- allHnNames.map(_._1).assertDistinct(s"Hn names must be distinct")
    yield allHnNames.toMap

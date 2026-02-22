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
| created: 2026-02-22 |||||||||||*/

package planning.engine.common.values.io

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.node.MnId.Con
import planning.engine.common.errors.{assertionError, assertDistinct}

final case class IoValueMap[F[_]: MonadThrow](
    valueMap: Map[IoValue, Set[Con]]
):
  lazy val isEmpty: Boolean = valueMap.isEmpty
  lazy val values: Iterable[Set[Con]] = valueMap.values
  lazy val keySet: Set[IoValue] = valueMap.keySet
  lazy val allMnIds: Set[Con] = valueMap.values.flatten.toSet

  def contains(ioValue: IoValue): Boolean = valueMap.contains(ioValue)

  def get(ioValue: IoValue): F[Set[Con]] = valueMap.get(ioValue) match
    case None      => s"No nodes id found for IoValue $ioValue".assertionError
    case Some(ids) => ids.pure

  private[io] def updateOrAddIoValues(ioValue: IoValue, mnId: Con): F[(IoValue, Set[Con])] = valueMap.get(ioValue) match
    case None                             => (ioValue -> Set(mnId)).pure
    case Some(ids) if !ids.contains(mnId) => (ioValue -> (ids + mnId)).pure
    case Some(ids)                        => s"Duplicate node id $mnId for IoValue $ioValue".assertionError

  def addIoValues(values: Iterable[(IoValue, Con)]): F[IoValueMap[F]] = values.toList
    .traverse((io, nId) => updateOrAddIoValues(io, nId))
    .map(newIoValues => this.copy(valueMap = valueMap ++ newIoValues.toMap))

object IoValueMap:
  def empty[F[_]: MonadThrow]: IoValueMap[F] = new IoValueMap(Map.empty)

  def apply[F[_]: MonadThrow](ioValues: Map[IoValue, Set[Con]]): F[IoValueMap[F]] =
    for
        _ <- ioValues.values.flatMap(_.toList).assertDistinct("Two or more IoValue cant refer to the same MnId")
    yield new IoValueMap(ioValues)

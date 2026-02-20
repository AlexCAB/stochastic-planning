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
| created: 2026-02-19 |||||||||||*/

package planning.engine.common.values.edge

import planning.engine.common.values.node.MnId

final case class EdgeKeySet[K <: EdgeKey](keys: Set[K]):
  lazy val srcIds: Set[MnId] = keys.map(_.src)
  lazy val trgIds: Set[MnId] = keys.map(_.trg)
  lazy val mnIds: Set[MnId] = srcIds ++ trgIds

  def contains(src: MnId, trg: MnId): Boolean = keys.exists(k => k.src == src && k.trg == trg)

object EdgeKeySet:
  def apply[K <: EdgeKey](keys: K*): EdgeKeySet[K] = EdgeKeySet(keys.toSet)

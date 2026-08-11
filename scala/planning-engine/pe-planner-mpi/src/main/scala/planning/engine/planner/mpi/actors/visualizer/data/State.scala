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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.data

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.common.repr.Representable

private[visualizer] final case class State(
    conNodes: Map[MnId.Con, Option[HnName]],
    absNodes: Map[MnId.Abs, Option[HnName]],
    srcLinkMap: Map[MnId, Set[Link.End]],
    srcThenMap: Map[MnId, Set[Then.End]],
    trgLinkMap: Map[MnId, Set[Link.End]],
    trgThenMap: Map[MnId, Set[Then.End]],
) extends Representable:

  // Replaces any node with the same ID that was already present.
  def withNodesAdded(ids: Map[MnId, Option[HnName]]): State =
    val newCon = ids.collect { case (id: MnId.Con, name) => id -> name }
    val newAbs = ids.collect { case (id: MnId.Abs, name) => id -> name }

    copy(conNodes = conNodes ++ newCon, absNodes = absNodes ++ newAbs)

  // Replaces any edge with the same key that was already present.
  def withEdgesAdded(keys: Set[MeKey]): State =
    def addEnd[E <: MeKey.End](map: Map[MnId, Set[E]], id: MnId, end: E): Map[MnId, Set[E]] =
      map.updated(id, map.getOrElse(id, Set.empty) + end)

    def addOne(st: State, key: MeKey): State = key match
      case l: Link => st.copy(
          srcLinkMap = addEnd(st.srcLinkMap, l.src, l.trgEnd),
          trgLinkMap = addEnd(st.trgLinkMap, l.trg, l.srcEnd),
        )
      case t: Then => st.copy(
          srcThenMap = addEnd(st.srcThenMap, t.src, t.trgEnd),
          trgThenMap = addEnd(st.trgThenMap, t.trg, t.srcEnd),
        )

    keys.foldLeft(this)(addOne)

private[visualizer] object State:
  val init: State = State(Map.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty)

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
| created: 2026-10-11 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node

final case class Lookup(
    inLink: Map[SampleId, Set[Node]],
    outLink: Map[SampleId, Set[Node]],
    inThen: Map[SampleId, Set[Node]],
    outThen: Map[SampleId, Set[Node]],
):
  private def find(m: Map[SampleId, Set[Node]], sId: Set[SampleId]): Set[Node] =
    m.filter(kv => sId.contains(kv._1)).values.flatten.toSet

  def findInLinks(sampleId: Set[SampleId]): Set[Node] = find(inLink, sampleId)
  def findOutLinks(sampleId: Set[SampleId]): Set[Node] = find(outLink, sampleId)
  def findInThen(sampleId: Set[SampleId]): Set[Node] = find(inThen, sampleId)
  def findOutThen(sampleId: Set[SampleId]): Set[Node] = find(outThen, sampleId)

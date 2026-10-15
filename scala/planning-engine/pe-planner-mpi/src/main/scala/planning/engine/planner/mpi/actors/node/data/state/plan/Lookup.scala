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

package planning.engine.planner.mpi.actors.node.data.state.plan

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.state.structure.Struct
import planning.engine.planner.mpi.actors.node.data.state.structure.Struct.EdgeData

final case class Lookup(
    incoming: Map[MnId, Struct.EdgeData],
    outgoing: Map[MnId, Struct.EdgeData],
):
  private def buildNodeMap(m: Map[MnId, EdgeData], p: EdgeType => Boolean): Map[MnId, Node] = m
    .values.filter(d => p(d.et)).map(d => d.neighbor.mnId -> d.neighbor).toMap

  lazy val inLink: Map[MnId, Node] = buildNodeMap(incoming, _.isLink)
  lazy val outLink: Map[MnId, Node] = buildNodeMap(outgoing, _.isLink)
  lazy val inThen: Map[MnId, Node] = buildNodeMap(incoming, _.isThen)
  lazy val outThen: Map[MnId, Node] = buildNodeMap(outgoing, _.isThen)

  private def buildSampleMap(m: Map[MnId, EdgeData], p: EdgeType => Boolean): Map[SampleId, Set[Node]] = m
    .values.filter(d => p(d.et))
    .flatMap(d => d.sampleIds.map(_ -> d.neighbor))
    .groupMap((sId, _) => sId)((_, neighbor) => neighbor)
    .view.mapValues(_.toSet).toMap

  lazy val inLinkSampleId: Map[SampleId, Set[Node]] = buildSampleMap(incoming, _.isLink)
  lazy val outLinkSampleId: Map[SampleId, Set[Node]] = buildSampleMap(outgoing, _.isLink)
  lazy val inThenSampleId: Map[SampleId, Set[Node]] = buildSampleMap(incoming, _.isThen)
  lazy val outThenSampleId: Map[SampleId, Set[Node]] = buildSampleMap(outgoing, _.isThen)

  private def find(m: Map[SampleId, Set[Node]], sId: Set[SampleId]): Set[Node] =
    m.filter(kv => sId.contains(kv._1)).values.flatten.toSet

  def findInLinks(sampleId: Set[SampleId]): Set[Node] = find(inLinkSampleId, sampleId)
  def findOutLinks(sampleId: Set[SampleId]): Set[Node] = find(outLinkSampleId, sampleId)
  def findInThen(sampleId: Set[SampleId]): Set[Node] = find(inThenSampleId, sampleId)
  def findOutThen(sampleId: Set[SampleId]): Set[Node] = find(outThenSampleId, sampleId)

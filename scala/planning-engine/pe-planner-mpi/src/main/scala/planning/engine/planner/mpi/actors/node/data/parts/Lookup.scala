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

package planning.engine.planner.mpi.actors.node.data.parts

import cats.syntax.ext.*
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node

final case class Lookup(incoming: Map[MnId, EdgeData], outgoing: Map[MnId, EdgeData]):
  private def buildNodeMap(m: Map[MnId, EdgeData], p: EdgeType => Boolean): Map[MnId, Node] = m
    .values.filter(d => p(d.et)).map(d => d.neighbor.mnId -> d.neighbor).toMap

  lazy val inLink: Map[MnId, Node] = buildNodeMap(incoming, _.isLink)
  lazy val outLink: Map[MnId, Node] = buildNodeMap(outgoing, _.isLink)
  lazy val inThen: Map[MnId, Node] = buildNodeMap(incoming, _.isThen)
  lazy val outThen: Map[MnId, Node] = buildNodeMap(outgoing, _.isThen)

  // For out edges with LINK type, target nodes must always have type Node.Abs.
  private def allAbsSet[F[_]: MT](m: Set[Node]): F[Set[Node.Abs]] = m.traverse:
    case node: Node.Abs => node.pure
    case node           => s"Expected Node.Abs, but got: $node".assertionError

  def allOutAbsLink[F[_]: MT]: F[Set[Node.Abs]] = allAbsSet(outLink.values.toSet)

  private def buildSampleMap(m: Map[MnId, EdgeData], p: EdgeType => Boolean): Map[SampleId, Set[Node]] = m
    .values.filter(d => p(d.et))
    .flatMap(d => d.sampleIds.map(_ -> d.neighbor))
    .groupMap((sId, _) => sId)((_, neighbor) => neighbor)
    .view.mapValues(_.toSet).toMap

  lazy val inLinkSampleId: Map[SampleId, Set[Node]] = buildSampleMap(incoming, _.isLink)
  lazy val outLinkSampleId: Map[SampleId, Set[Node]] = buildSampleMap(outgoing, _.isLink)
  lazy val inThenSampleId: Map[SampleId, Set[Node]] = buildSampleMap(incoming, _.isThen)
  lazy val outThenSampleId: Map[SampleId, Set[Node]] = buildSampleMap(outgoing, _.isThen)

  // Active edges is the edges that have active samples
  private def findActive(m: Map[SampleId, Set[Node]], activeSampleIds: Set[SampleId]): Set[Node] =
    m.filter(kv => activeSampleIds.contains(kv._1)).values.flatten.toSet

  def findInLink(sampleId: Set[SampleId]): Set[Node] = findActive(inLinkSampleId, sampleId)

  def findOutAbsLink[F[_]: MT](sampleId: Set[SampleId]): F[Set[Node.Abs]] =
    allAbsSet(findActive(outLinkSampleId, sampleId))

  def findInThen(sampleId: Set[SampleId]): Set[Node] = findActive(inThenSampleId, sampleId)
  def findOutThen(sampleId: Set[SampleId]): Set[Node] = findActive(outThenSampleId, sampleId)

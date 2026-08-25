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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.node.MnId.Nim
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[manager] final case class State(
    // Next ID to assign to a new entities (incremented for each new entity)
    // In simple implementation `nextSampleId` and `nextHnIndexies` is in Manager state.
    // But in future `nextSampleId` it can be moved to a separate actor which will handle samples adding.
    nextMnId: Long,
    nextSampleId: Long,

    // List of all node in map network
    nodeRefMap: Map[MnId, Node],

    // Mapping from node names to node IDs. Used for finding nodes by name.
    nodeNameMap: Map[HnName, Set[MnId]],

    // List of all samples in map network.
    // In simple implementation it is in Manager state, but in future there should be some separate storage for it.
    sampleDataMap: Map[SampleId, State.SampleData],
) extends Representable:
  import State.*

  def withNewNodes[F[_]: MonadThrow](
      data: Map[Nim, NodeData],
      spawn: (Long, NodeData) => F[Node],
  ): F[(Map[Nim, Node], State)] =
    def spawnAllNode: F[List[(Nim, Node)]] =
      data.zipWithIndex.toList.traverse((e, i) => spawn(nextMnId + i, e._2).map(n => e._1 -> n))

    def extractNames(nodes: List[Node]): Map[HnName, Set[MnId]] = nodes
      .collect { case n if n.name.isDefined => n.name.get -> n.mnId }
      .groupBy(_._1).map((name, ids) => name -> (ids.map(_._2).toSet ++ nodeNameMap.getOrElse(name, Set.empty)))

    def updateState(nodes: List[Node]): State = this.copy(
      nodeRefMap = nodeRefMap ++ nodes.map(n => n.mnId -> n),
      nodeNameMap = nodeNameMap ++ extractNames(nodes),
      nextMnId = nextMnId + nodes.size,
    )

    for
      nodes <- spawnAllNode
      mnIds = nodes.map(_._2.mnId)
      _ <- mnIds.assertDistinct("Duplicate node IDs in new nodes")
      _ <- nodeRefMap.keySet.assertContainsNoneOf(mnIds, "Node IDs already exist in the current state")
    yield (nodes.toMap, updateState(nodes.map(_._2)))

  def withNewManSamples[F[_]: MonadThrow](
    samples: Set[Sample.Man],
  ): F[(Map[SampleId, Sample.Man], State)] =
    def updateState(samples: Map[SampleId, SampleData]): State = this.copy(
      sampleDataMap = sampleDataMap ++ samples,
      nextSampleId = nextSampleId + samples.size,
    )

    for
      withIds <- samples.zipWithIndex.map((d, i) => SampleId(nextSampleId + i) -> d).toMap.pure
      _ <- sampleDataMap.keySet.assertContainsNoneOf(withIds.keySet, "Sample IDs already exist in the current state")
      withData = withIds.map((id, sample) => id -> SampleData(sample.props, Some(sample.info)))
    yield (withIds, updateState(withData))

  def getNode[F[_]: MonadThrow](id: MnId): F[Node] = nodeRefMap.get(id) match
    case Some(node) => node.pure
    case None       => s"Node ID $id not found in state".assertionError

  def getSamples[F[_]: MonadThrow](ids: Set[SampleId]): F[Map[SampleId, State.SampleData]] =
    for
        _ <- sampleDataMap.keySet.assertContainsAllOf(ids, "Some sample IDs not found in state")
    yield sampleDataMap.view.filterKeys(ids.contains).toMap

  def findByName[F[_]: MonadThrow](name: HnName): F[Option[Node]] = nodeNameMap.get(name) match
    case Some(ids) if ids.size == 1 => getNode(ids.head).map(Some(_))
    case Some(ids) => s"Expected exactly one node ID for name '$name', got: ${ids.mkString(", ")}".assertionError
    case None      => None.pure

private[manager] object State:
  final case class SampleData(props: Sample.Props, info: Option[Sample.Info])

  val init: State = State(1L, 1L, Map.empty, Map.empty, Map.empty)

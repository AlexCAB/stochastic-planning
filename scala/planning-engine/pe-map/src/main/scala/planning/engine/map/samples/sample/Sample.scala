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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.samples.sample

import cats.MonadThrow
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import cats.syntax.all.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.errors.assertionError
import planning.engine.common.values.StringVal.toStr
import planning.engine.common.errors.*

final case class Sample(
    data: SampleData,
    edges: Set[SampleEdge]
) extends Validation:
  lazy val allHnIds: Set[HnId] = edges.flatMap(e => Set(e.source.hnId, e.target.hnId))

  lazy val validationName: String = s"Sample(id=${data.id}, name=${data.name.toStr})"

  lazy val validationErrors: List[Throwable] =
    val edgeIds = edges.map(e => (e.source.hnId, e.target.hnId))
    val indexies = edges.flatMap(e => List(e.source, e.target)).groupBy(_.hnId).view.mapValues(_.map(_.value)).toMap
    val invalidIndexies = indexies.filter((_, v) => v.size > 1)

    val nodesNeighbours = (edgeIds ++ edgeIds.flatMap(e => Set((e._2, e._1))))
      .groupBy(_._1).view.mapValues(_.map(_._2))
      .toMap

    def findConnectedLoop(start: HnId, visited: Set[HnId]): Set[HnId] = nodesNeighbours(start)
      .foldLeft(visited + start)((acc, nId) => if acc.contains(nId) then acc else findConnectedLoop(nId, acc))

    val connectedNodes = if allHnIds.nonEmpty then findConnectedLoop(allHnIds.head, Set()) else Set()
    validations(
      // Sample without edges no sense since by design it must reflect an relationship between HNs
      edges.nonEmpty -> "At least one SampleEdge must be defined",
      invalidIndexies.isEmpty -> s"Conflicting HnIndex values for: $invalidIndexies",
      edges.map(_.sampleId).haveSameElems(Set(data.id), "All SampleEdges must have the same SampleId as SampleData"),
      connectedNodes.haveSameElems(allHnIds, "All HnIds must be connected in the Sample edges")
    )

  override lazy val toString: String = "Sample(" +
    s"id = ${data.id.value}, " +
    s"name = ${data.name.toStr}, " +
    s"count = ${data.probabilityCount}, " +
    s"utility = ${data.utility}, " +
    s"edges = [${edges.map(_.toString).mkString(", ")}])"

object Sample:
  final case class New(
      probabilityCount: Long,
      utility: Double,
      name: Option[Name],
      description: Option[Description],
      edges: Set[SampleEdge.New]
  ) extends Validation:
    lazy val hnIds: Set[HnId] = edges.flatMap(e => List(e.source, e.target))

    lazy val validationName: String =
      s"Sample.New(name=${name.toStr}, probabilityCount=$probabilityCount, utility=$utility)"

    lazy val validationErrors: List[Throwable] = validations(
      (probabilityCount > 0) -> "Probability count must be greater than 0",
      name.forall(_.value.nonEmpty) -> "Name must not be empty if defined",
      description.forall(_.value.nonEmpty) -> "Description must not be empty if defined",
      edges.nonEmpty -> "At least one edge must be provided"
    )

    def findHnIndexies[F[_]: MonadThrow](hnInsToHnIndex: Map[HnId, List[HnIndex]])
        : F[(Map[HnId, List[HnIndex]], Map[HnId, HnIndex])] = hnIds
      .foldRight((Map[HnId, List[HnIndex]](), Map[HnId, HnIndex]()).pure):
        case (hnId, acc) => hnInsToHnIndex.get(hnId) match
            case Some(inx :: hnIxs) => acc.map((ixs, ix) => (ixs + (hnId -> hnIxs), ix + (hnId -> inx)))
            case _ => s"Missing HnIndex for hnId = $hnId, in hnInsToHnIndex = $hnInsToHnIndex".assertionError
      .map((ixs, ix) => (ixs ++ hnInsToHnIndex.filterNot((id, _) => hnIds.contains(id)), ix))

    def toQueryParams[F[_]: MonadThrow](sampleId: SampleId): F[Map[String, Param]] = paramsOf(
      PROP.SAMPLE_ID -> sampleId.toDbParam,
      PROP.PROBABILITY_COUNT -> probabilityCount.toDbParam,
      PROP.UTILITY -> utility.toDbParam,
      PROP.NAME -> name.map(_.toDbParam),
      PROP.DESCRIPTION -> description.map(_.toDbParam)
    )

    def toSampleData(id: SampleId): SampleData = SampleData(
      id = id,
      probabilityCount = probabilityCount,
      utility = utility,
      name = name,
      description = description
    )

    override lazy val toString: String = s"Sample.New(" +
      s"probabilityCount = $probabilityCount, " +
      s"utility = $utility, " +
      s"name = ${name.toStr}, " +
      s"description = ${description.toStr}, " +
      s"hnIds = [${hnIds.map(_.value).mkString(", ")}], " +
      s"edges = [${edges.map(_.toString).mkString(", ")}])"

  final case class ListNew(list: List[New]):
    lazy val allEdges: List[SampleEdge.New] = list.flatMap(_.edges).distinct
    lazy val allHnIds: List[HnId] = allEdges.flatMap(e => List(e.source, e.target)).distinct
    lazy val numHnIndexPerHn: Map[HnId, Int] = allHnIds.map(id => id -> list.count(_.hnIds.contains(id))).toMap

    def appendAll(samples: ListNew): ListNew = ListNew(list ++ samples.list)

  object ListNew:
    def of(samples: New*): ListNew = ListNew(samples.toList)

  def formNew[F[_]: MonadThrow](
      id: SampleId,
      newData: Sample.New,
      hnIndexes: Map[HnId, HnIndex]
  ): F[Sample] =
    for
      edges <- newData.edges.toList.traverse(e => SampleEdge.fromNew[F](id, e, hnIndexes))
      _ <- edges.assertDistinct(s"Duplicate edges found in sample id = $id")
      data = newData.toSampleData(id)
    yield Sample(data = data, edges = edges.toSet)

  def formDataMap[F[_]: MonadThrow](
      id: SampleId,
      sampleDataMap: Map[SampleId, SampleData],
      edgesMap: Map[SampleId, List[SampleEdge]]
  ): F[Sample] =
    for
      data <- sampleDataMap.get(id).map(_.pure).getOrElse(s"SampleData not found for $id".assertionError)
      edges <- edgesMap.get(id).map(_.pure).getOrElse(s"SampleEdges not found for $id".assertionError)
      _ <- edges.assertDistinct(s"Duplicate edges found in sample id = $id")
    yield Sample(data = data, edges = edges.toSet)

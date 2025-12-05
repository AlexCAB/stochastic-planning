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
| created: 2025-07-08 |||||||||||*/

package planning.engine.api.model.map

import cats.MonadThrow
import planning.engine.api.model.map.payload.{
  AbstractNodeDef,
  ConcreteNodeDef,
  HiddenNodeDef,
  NewSampleData,
  NewSampleEdge
}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.common.errors.assertionError
import io.circe.{Decoder, Encoder}
import cats.syntax.all.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.io.IoName
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode

final case class MapAddSamplesRequest(
    samples: List[NewSampleData],
    hiddenNodes: List[HiddenNodeDef] // This is a list of hidden nodes used in the samples, should be unique
) extends Validation:

  lazy val hnNames: List[HnName] = hiddenNodes.map(_.name)
  lazy val validationName: String = "MapAddSamplesRequest"

  private lazy val hnNamesSet = hnNames.toSet

  lazy val validationErrors: List[Throwable] = validations(
    (hiddenNodes.map(_.name).distinct.size == hiddenNodes.size) -> "Hidden nodes names must be unique",
    hiddenNodes.nonEmpty -> "Hidden nodes names must not be empty"
  ) ++ validations(samples.map(s =>
    s.edgesHnNames.subsetOf(
      hnNamesSet
    ) -> s"Sample edges must reference only provided hnNames: ${s.edgesHnNames} not in $hnNames"
  )*)

  def listNewNotFoundHn[F[_]: MonadThrow](
      foundHnNames: Set[HnName],
      getIoNode: IoName => F[IoNode[F]]
  ): F[(ConcreteNode.ListNew, AbstractNode.ListNew)] =
    val hns = hiddenNodes.filterNot(hn => foundHnNames.contains(hn.name))

    val (conHns, absHns) = hns.foldRight((List[ConcreteNodeDef](), List[AbstractNodeDef]())):
      case (n: ConcreteNodeDef, (conList, absList)) => (n +: conList, absList)
      case (n: AbstractNodeDef, (conList, absList)) => (conList, n +: absList)

    conHns.traverse(_.toNew(getIoNode)).map: newConHns =>
      (ConcreteNode.ListNew(newConHns), AbstractNode.ListNew(absHns.map(_.toNew)))

  def toSampleNewList[F[_]: MonadThrow](hnIdMap: Map[HnName, HnId]): F[Sample.ListNew] =
    def getHnId(hnName: HnName): F[HnId] = hnIdMap.get(hnName) match
      case Some(id) => id.pure
      case _        => s"HnName $hnName not found in hnIdMap: $hnIdMap".assertionError

    def makeEdge(raw: NewSampleEdge): F[SampleEdge.New] =
      for
        sourceHnIds <- getHnId(raw.sourceHnName)
        targetHnIds <- getHnId(raw.targetHnName)
      yield SampleEdge.New(source = sourceHnIds, target = targetHnIds, edgeType = raw.edgeType)

    def makeSample(raw: NewSampleData, edges: List[SampleEdge.New]): Sample.New = Sample
      .New(
        probabilityCount = raw.probabilityCount,
        utility = raw.utility,
        name = raw.name,
        description = raw.description,
        edges = edges
      )

    samples
      .traverse(raw => raw.edges.traverse(makeEdge).map(edges => makeSample(raw, edges)))
      .map(sl => Sample.ListNew(sl))

object MapAddSamplesRequest:
  import io.circe.generic.semiauto.*

  implicit val decoder: Decoder[MapAddSamplesRequest] = deriveDecoder[MapAddSamplesRequest]
  implicit val encoder: Encoder[MapAddSamplesRequest] = deriveEncoder[MapAddSamplesRequest]

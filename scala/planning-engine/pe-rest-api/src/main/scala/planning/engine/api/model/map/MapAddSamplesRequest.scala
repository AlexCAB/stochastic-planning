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
import planning.engine.api.model.map.payload.{AbstractNodeDef, ConcreteNodeDef, NewSampleData, NewSampleEdge}
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.common.errors.assertionError
import io.circe.{Decoder, Encoder}
import cats.syntax.all.*
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}

final case class MapAddSamplesRequest(
    samples: List[NewSampleData]
):
  val hnNames: List[Name] = samples.flatMap(_.hiddenNodes.map(_.name)).distinct

  def listNewNotFoundHn(foundHnNames: Set[Name]): (ConcreteNode.ListNew, AbstractNode.ListNew) =
    val hns = samples.flatMap(_.hiddenNodes).filterNot(hn => foundHnNames.contains(hn.name))
    val (conHns, absHns) = hns.foldRight((List[ConcreteNodeDef](), List[AbstractNodeDef]())):
      case (n: ConcreteNodeDef, (conList, absList)) => (n +: conList, absList)
      case (n: AbstractNodeDef, (conList, absList)) => (conList, n +: absList)
    (ConcreteNode.ListNew(conHns.map(_.toNew)), AbstractNode.ListNew(absHns.map(_.toNew)))

  def toSampleNewList[F[_]: MonadThrow](hnIdMap: Map[Name, HnId]): F[Sample.ListNew] =
    def getHnId(hnName: Name): F[HnId] = hnIdMap.get(hnName) match
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

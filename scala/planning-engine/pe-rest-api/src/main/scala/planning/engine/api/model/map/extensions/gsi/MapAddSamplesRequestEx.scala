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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.errors.assertionError
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.api.model.map.MapAddSamplesRequest
import planning.engine.api.model.map.extensions.gsi.ConcreteNodeDefEx.toNew
import planning.engine.api.model.map.extensions.gsi.AbstractNodeDefEx.toNew

object MapAddSamplesRequestEx:
  extension (request: MapAddSamplesRequest)
    def listNewNotFoundHn[F[_]: MT](
        foundHnNames: Set[HnName],
        getIoNode: IoName => F[IoNode[F]],
    ): F[(ConcreteNode.ListNew, AbstractNode.ListNew)] =
      val hns = request.hiddenNodes.filterNot(hn => foundHnNames.contains(hn.name))

      val (conHns, absHns) = hns.foldRight((List[ConcreteNodeDef](), List[AbstractNodeDef]())):
        case (n: ConcreteNodeDef, (conList, absList)) => (n +: conList, absList)
        case (n: AbstractNodeDef, (conList, absList)) => (conList, n +: absList)

      conHns.traverse(_.toNew(getIoNode)).map: newConHns =>
        (ConcreteNode.ListNew(newConHns), AbstractNode.ListNew(absHns.map(_.toNew)))

    def toSampleNewList[F[_]: MT](hnIdMap: Map[HnName, HnId]): F[Sample.ListNew] =
      def getHnId(hnName: HnName): F[HnId] = hnIdMap.get(hnName) match
        case Some(id) => id.pure
        case _        => s"HnName $hnName not found in hnIdMap: $hnIdMap".assertionError

      def makeEdge(raw: NewSampleEdge): F[SampleEdge.New] =
        for
          sourceHnIds <- getHnId(raw.sourceHnName)
          targetHnIds <- getHnId(raw.targetHnName)
        yield SampleEdge.New(source = sourceHnIds, target = targetHnIds, edgeType = raw.edgeType)

      def makeSample(raw: NewSampleData, edges: Set[SampleEdge.New]): Sample.New = Sample
        .New(
          probabilityCount = raw.probabilityCount,
          utility = raw.utility,
          name = raw.name,
          description = raw.description,
          edges = edges,
        )

      request.samples
        .traverse(raw => raw.edges.traverse(makeEdge).map(edges => makeSample(raw, edges.toSet)))
        .map(sl => Sample.ListNew(sl))

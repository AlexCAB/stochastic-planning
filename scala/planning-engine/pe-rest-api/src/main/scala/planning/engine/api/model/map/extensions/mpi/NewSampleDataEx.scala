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

package planning.engine.api.model.map.extensions.mpi

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.api.model.map.payload.{NewSampleData, NewSampleEdge}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.MeKey

object NewSampleDataEx:
  extension (data: NewSampleData)
    def toSample[F[_]: MT](mnIds: Map[HnName, MnId]): F[Sample.Man] =
      def makeProps = Sample.Props(
        probabilityCount = data.probabilityCount,
        utility = data.utility,
      )

      def makeInfo = Sample.Info(
        name = data.name.getOrElse(Name(s"Unnamed Sample: P(${data.probabilityCount}), U(${data.utility})")),
        description = data.description,
      )

      def getMnId(hnName: HnName): F[MnId] = mnIds.get(hnName) match
        case Some(id) => id.pure
        case None     => s"HnName $hnName not found in mnIds: $mnIds".assertionError

      def makeEdge(e: NewSampleEdge): F[MeKey] =
        for
          srcId <- getMnId(e.sourceHnName)
          trgId <- getMnId(e.targetHnName)
        yield MeKey(e.edgeType, srcId, trgId)

      for
        edges <- data.edges.traverse(makeEdge)
        _ <- edges.assertDistinct("Sample edges must be distinct, but found duplicates.")
        props = makeProps
        info = makeInfo
      yield Sample.Man(props, info, edges.toSet)

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

import cats.MonadThrow
import planning.engine.api.model.map.MapAddSamplesRequest
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample

object MapAddSamplesRequestEx:
    extension (request: MapAddSamplesRequest)
      def toNodes[F[_]: MonadThrow]: F[(Map[HnName, MnId], Map[MnId.Nim, NodeData])] = ???

      def toSamples[F[_]: MonadThrow](mnIds: Map[HnName, MnId]): F[Set[Sample.Man]] = ???



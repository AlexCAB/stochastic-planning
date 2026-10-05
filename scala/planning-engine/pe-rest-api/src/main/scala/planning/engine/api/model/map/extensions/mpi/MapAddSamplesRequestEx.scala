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
import cats.syntax.ext.*
import planning.engine.api.model.map.MapAddSamplesRequest
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.common.errors.assertDistinct

object MapAddSamplesRequestEx:
  import HiddenNodeDefEx.*, NewSampleDataEx.*, MnId.Nim

  extension (request: MapAddSamplesRequest)
    def toNodes[F[_]: MT](vars: IoVars): F[(Map[HnName, MnId], Map[MnId.Nim, NodeData])] = request
      .hiddenNodes.zipWithIndex
      .traverse((n, i) => n.toNodeData(vars).map(d => Nim(i) -> (n.name, d)))
      .map(ds => (ds.map((id, d) => d._1 -> id).toMap, ds.map((id, d) => id -> d._2).toMap))

    def toSamples[F[_]: MT](mnIds: Map[HnName, MnId]): F[Set[Sample.Man]] =
      for
        samples <- request.samples.traverse(_.toSample(mnIds))
        _ <- samples.assertDistinct("Samples must be distinct, but found duplicates.")
      yield samples.toSet

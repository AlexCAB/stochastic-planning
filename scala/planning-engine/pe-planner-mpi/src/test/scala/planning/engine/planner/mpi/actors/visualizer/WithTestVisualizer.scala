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
| created: 14.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import cats.MonadThrow
import cats.syntax.ext.MT
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.IoVars

trait WithTestVisualizer:
  self: UnitSpecWithIOAndTestKit =>

  trait WithVisualizer:
    lazy val testMetadata: Metadata = Metadata(Name("test-map"), None)
    lazy val testVars: IoVars = new IoVars(Map.empty, Map.empty)

    lazy val noOpVisualization: Visualization = new Visualization:
      override def init[F[_]: MT](metadata: Metadata, variables: IoVars): F[Unit] = MonadThrow[F].unit
      override def nodesAdded[F[_]: MT](ids: Map[MnId, Option[HnName]]): F[Unit] = MonadThrow[F].unit
      override def edgesAdded[F[_]: MT](keys: Set[MeKey]): F[Unit] = MonadThrow[F].unit

    def makeVisualizer(
        metadata: Metadata = testMetadata,
        vars: IoVars = testVars,
        viz: Visualization = noOpVisualization,
    ): TestVisualizer = TestVisualizer(metadata, vars, viz, "test-visualizer")

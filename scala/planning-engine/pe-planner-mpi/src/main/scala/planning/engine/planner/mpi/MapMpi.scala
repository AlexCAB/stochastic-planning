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
| created: 10.06.2026 |||||||||||*/

package planning.engine.planner.mpi

import cats.effect.std.AtomicCell
import cats.effect.{Async, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.guardian.Guardian
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.io.Variable
import planning.engine.planner.mpi.map.MapMpiImpl
import planning.engine.planner.mpi.model.data.map.Metadata

// Map actor adaptor, main purpose:
// - Spawn and host actors network
// - Adapting actors network to REST API exposed to the Python tools
//
// Actors parent-child graph:
//                            ┌──────────┐
//                            │ Guardian │
//                            └─────┬────┘
//                 ┌────────────────┼───────────────┐
//                 │                │               │
//           ┌─────┴─────┐   ┌──────┴──────┐  ┌─────┴─────┐
//           │  manager  │   │ visualizer  │  │  planner  │
//           └─────┬─────┘   └─────────────┘  └───────────┘
//                 │
//      ┌──────────┼─────────────┐
//      │          │             │
// ┌────┴───┐ ┌────┴───┐     ┌───┴────┐
// │ node 1 │ │ node 2 │  …  │ node N │
// └────────┘ └────────┘     └────────┘
//
// Actors dependency graph (i.e. by actors which passed via constructor):
//                 ┌──────────┐
//                 │ manager  │
//                 └──▲──┬──▲─┘
//       ┌────────────┘  │  └────────────┐
//       │               │               │
// ┌─────┴──────┐        │        ┌──────┴──────┐
// │ visualizer │        │        │   planner   │
// └─────┬──────┘        │        └──────┬──────┘
//       │               │               │
//       │          ┌────▼───┐           │
//       └─────────►│  node  │◄──────────┘
//                  └────────┘
//
// Initial implementation will be as simple as possible:
// - Use a single actor for managing the graph (nodes and edges).
// - Only add and update operations for nodes and edges will be supported.
// - No error handling or recovery will be implemented in the initial version (any errors will terminate the system,
//   and experiment have to be restarted manually).
trait MapMpi[F[_]]:
  // Initialize the map network with given input and output variables, and optional visualization.
  def init(metadata: Metadata, inVars: Set[Variable.Input], outVars: Set[Variable.Output]): F[Unit]

  // Clean up the map network, stopping all actors and releasing resources.
  def reset(): F[Unit]

  // Add manually defined samples to the map network, associating them with the specified nodes.
  def addSamples(samples: Set[Sample.Man], nodes: Map[MnId.Nim, NodeData]): F[Map[SampleId, Sample.Man]]

object MapMpi:
  def apply[F[_]: {Async, LoggerFactory}](visualization: Option[Visualization]): Resource[F, MapMpi[F]] =
    for
      (guardian, scheduler) <- Guardian.create()
      actors <- Resource.eval(AtomicCell[F].of(Option.empty[MapMpiImpl.MapState]))
    yield new MapMpiImpl[F](visualization, guardian, scheduler, actors)

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

package planning.engine.planner.mpi.map

import cats.effect.Sync
import cats.effect.Resource

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
trait MapMpi

private[map] class MapMpiLike extends MapMpi

// TODO Adopt functional API to Actors, have state where save actors references

object MapMpi:

  def apply[F[_]: Sync](): Resource[F, MapMpi] = ???

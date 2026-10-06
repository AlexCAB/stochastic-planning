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
| created: 2026-09-09 |||||||||||*/

package planning.engine.planner.mpi

import cats.syntax.ext.MT
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}

// Map network visualization interface:
// - External component should provide implementation of this trait to `MapMpi`
//   to receive updates about the structure and plan of the map network.
// - The methods will be called with in visualizer actor (`mpi.actors.visualizer.logic.Actor`) execution context,
//   so the implementation should be thread-safe and non-blocking.
// - Any exceptions thrown from the implementation will lead to full system termination,
//   because missing if some update events (in case it just logged and ignored) will lead to inconsistent state
//   of the visualization.
// - Visualizer actor is stateless (it just aggregate map network events and re-directs them to `Visualization`),
//   so visualization should maintain its own state if needed.
trait Visualization:
  // Called when new hidden nodes added to the map network structure.
  def nodesAdded[F[_]: MT](ids: Map[MnId, Option[HnName]]): F[Unit]

  // Called when new edges added to the map network structure.
  def edgesAdded[F[_]: MT](keys: Set[MeKey]): F[Unit]

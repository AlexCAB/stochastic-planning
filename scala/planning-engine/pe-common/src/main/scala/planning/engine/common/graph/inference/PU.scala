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
| created: 2026-10-10 |||||||||||*/

package planning.engine.common.graph.inference

// P(N) and U(N) values for a node N in the graph,
// where P is the probability of the node,
// and U is the utility of the node.
final case class PU(p: Double, u: Double)

object PU:
  val zero: PU = PU(0.0, 0.0)

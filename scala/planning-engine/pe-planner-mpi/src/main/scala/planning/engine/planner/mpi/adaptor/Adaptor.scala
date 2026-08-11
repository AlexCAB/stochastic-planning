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

package planning.engine.planner.mpi.adaptor


// Map actor adaptor, main purpose:
// - Spawn and host actors network 
// - Adapting actors network to REST API exposed to the Python tools
//
// Initial implementation will be as simple as possible:
// - Use a single actor for managing the graph (nodes and edges).
// - Only add and update operations for nodes and edges will be supported.
// - No error handling or recovery will be implemented in the initial version (any errors will terminate the system,
//   and experiment have to be restarted manually).
trait Adaptor


object Adaptor:

  def apply(): Adaptor = ???

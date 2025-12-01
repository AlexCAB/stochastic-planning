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
| created: 2025-11-30 |||||||||||*/

package planning.engine.planner.map

import cats.effect.kernel.Async
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.dcg.nodes.ConcreteMapNode

trait SimpleMapLike[F[_]]:
  def loadForIoValues(values: Map[Name, IoIndex]): F[List[ConcreteMapNode[F]]]
  
  
class SimpleMap[F[_]: {Async, LoggerFactory}](mapGraph: MapGraphLake[F]):
  def loadForIoValues(values: Map[Name, IoIndex]): F[List[ConcreteMapNode[F]]] = ???

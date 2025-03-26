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
| created: 2025-03-25 |||||||||||*/


package planning.engine.core.map.io.node

import cats.MonadThrow
import neotypes.model.types.Value
import planning.engine.core.map.io.variable.IoVariable


trait IoNode[F[_] : MonadThrow, T]:
  val name: String
  val variable: IoVariable[F, T]

  def toProperties: F[Map[String, Value]] = ???

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"


object IoNode:
  def fromProperties[F[_] : MonadThrow](properties: Map[String, Value]): F[IoNode[F, ?]] = ???

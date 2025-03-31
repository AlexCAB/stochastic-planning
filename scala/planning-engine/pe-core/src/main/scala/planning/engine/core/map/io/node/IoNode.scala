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
import cats.syntax.all.*
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*

trait IoNode[F[_]: MonadThrow]:
  val name: String
  val variable: IoVariable[F, ?]

  private def nodeType: F[Value] = this match
    case _: InputNode[?]  => Value.Str(InputNode.propertyNodeType).pure
    case _: OutputNode[?] => Value.Str(OutputNode.propertyNodeType).pure
    case n                => s"Unknown node type: ${n.getClass.getSimpleName}".assertionError

  def toProperties: F[Map[String, Value]] =
    propsOf("type" -> nodeType, "name" -> Value.Str(name), "variable" -> variable.toProperties)

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"

object IoNode:
  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[IoNode[F]] =
    for
      nodeType <- properties.getValue[F, String]("type")
      name <- properties.getValue[F, String]("name")
      variable <- properties.getProps("variable").flatMap(IoVariable.fromProperties[F])

      ioNode <- nodeType match
        case t if t.equalsIgnoreCase(InputNode.propertyNodeType)  => InputNode[F](name, variable).pure
        case t if t.equalsIgnoreCase(OutputNode.propertyNodeType) => OutputNode[F](name, variable).pure
        case t                                                    => s"Unknown node type: $t".assertionError
    yield ioNode

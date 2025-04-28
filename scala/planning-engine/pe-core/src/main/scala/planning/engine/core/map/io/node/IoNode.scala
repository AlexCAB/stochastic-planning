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
import cats.effect.kernel.Concurrent
import neotypes.model.types.Value
import planning.engine.core.map.io.variable.IoVariable
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import cats.effect.std.AtomicCell
import planning.engine.common.values.{Index, Name}
import planning.engine.core.map.hidden.node.ConcreteNode
import cats.syntax.all.*

type ConcreteNodeMap[F[_]] = Map[Index, Vector[ConcreteNode[F]]]

trait IoNode[F[_]: MonadThrow]:
  val name: Name
  val variable: IoVariable[F, ?]

  protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]

  private def nodeType: F[Value] = this match
    case _: InputNode[?]  => Value.Str(InputNode.propertyNodeType).pure
    case _: OutputNode[?] => Value.Str(OutputNode.propertyNodeType).pure
    case n                => s"Unknown node type: ${n.getClass.getSimpleName}".assertionError

  private[core] def addConcreteNode(n: ConcreteNode[F]): F[ConcreteNode[F]] = hiddenNodes
    .update:
      case ns if ns.contains(n.valueIndex) => ns.updated(n.valueIndex, ns(n.valueIndex) :+ n)
      case ns                              => ns.updated(n.valueIndex, Vector(n))
    .as(n)

  private[core] def getAllConcreteNode: F[ConcreteNodeMap[F]] = hiddenNodes.get

  def toProperties: F[Map[String, Value]] =
    propsOf("type" -> nodeType, "name" -> Value.Str(name.value), "variable" -> variable.toProperties)

  override def equals(obj: Any): Boolean = (obj, this) match
    case (that: InputNode[?], self: InputNode[?]) => self.name == that.name && self.variable == that.variable
    case (that: OutputNode[?], self: OutputNode[?]) => self.name == that.name && self.variable == that.variable
    case _ => false

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"

object IoNode:
  def fromProperties[F[_]: Concurrent](properties: Map[String, Value]): F[IoNode[F]] =
    for
      nodeType <- properties.getValue[F, String]("type")
      name <- properties.getValue[F, String]("name")
      variable <- properties.getProps("variable").flatMap(IoVariable.fromProperties[F])

      ioNode <- nodeType match
        case t if t.equalsIgnoreCase(InputNode.propertyNodeType) =>
          InputNode[F](Name(name), variable).map(_.asInstanceOf[IoNode[F]])

        case t if t.equalsIgnoreCase(OutputNode.propertyNodeType) =>
          OutputNode[F](Name(name), variable).map(_.asInstanceOf[IoNode[F]])

        case t => s"Unknown node type: $t".assertionError
    yield ioNode

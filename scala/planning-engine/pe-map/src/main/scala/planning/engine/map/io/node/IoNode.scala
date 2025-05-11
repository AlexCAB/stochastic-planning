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

package planning.engine.map.io.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import neotypes.model.types.{Node, Value}
import planning.engine.common.errors.assertionError
import cats.effect.std.AtomicCell
import planning.engine.common.values.{Index, Name}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.variable.IoVariable
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.map.database.Neo4jQueries.IO_NODE_LABEL
import planning.engine.map.io.node.IoNode.*

type ConcreteNodeMap[F[_]] = Map[Index, Vector[ConcreteNode[F]]]

trait IoNode[F[_]: MonadThrow]:
  val name: Name
  val variable: IoVariable[F, ?]

  protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]

  private def nodeType: F[String] = this match
    case _: InputNode[?]  => InputNode.IN_NODE_TYPE.pure
    case _: OutputNode[?] => OutputNode.OUT_NODE_TYPE.pure
    case n                => s"Unknown node type: ${n.getClass.getSimpleName}".assertionError

  private[map] def addConcreteNode(n: ConcreteNode[F]): F[ConcreteNode[F]] = hiddenNodes
    .update:
      case ns if ns.contains(n.valueIndex) => ns.updated(n.valueIndex, ns(n.valueIndex) :+ n)
      case ns                              => ns.updated(n.valueIndex, Vector(n))
    .as(n)

  private[map] def getAllConcreteNode: F[ConcreteNodeMap[F]] = hiddenNodes.get

  def toQueryParams: F[Map[String, Param]] = paramsOf(
    IO_TYPE_PROP_NAME -> nodeType.map(t => t.toDbParam),
    NAME_PROP_NAME -> name.value.toDbParam,
    VARIABLE_PROP_NAME -> variable.toQueryParams
  )

  override def equals(obj: Any): Boolean = (obj, this) match
    case (that: InputNode[?], self: InputNode[?])   => self.name == that.name && self.variable == that.variable
    case (that: OutputNode[?], self: OutputNode[?]) => self.name == that.name && self.variable == that.variable
    case _                                          => false

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"

object IoNode:
  val IO_TYPE_PROP_NAME = "io_type"
  val NAME_PROP_NAME = "name"
  val VARIABLE_PROP_NAME = "variable"

  def fromProperties[F[_]: Concurrent](properties: Map[String, Value]): F[IoNode[F]] =
    for
      nodeType <- properties.getValue[F, String](IO_TYPE_PROP_NAME)
      name <- properties.getValue[F, String](NAME_PROP_NAME)
      variable <- properties.getProps(VARIABLE_PROP_NAME).flatMap(IoVariable.fromProperties[F])

      ioNode <- nodeType match
        case t if t.equalsIgnoreCase(InputNode.IN_NODE_TYPE) =>
          InputNode[F](Name(name), variable).map(_.asInstanceOf[IoNode[F]])

        case t if t.equalsIgnoreCase(OutputNode.OUT_NODE_TYPE) =>
          OutputNode[F](Name(name), variable).map(_.asInstanceOf[IoNode[F]])

        case t => s"Unknown node type: $t".assertionError
    yield ioNode

  def fromNode[F[_]: Concurrent](node: Node): F[IoNode[F]] = node match
    case Node(_, labels, props) if labels.exists(_.equalsIgnoreCase(IO_NODE_LABEL)) => fromProperties[F](props)
    case _ => s"Not a IO node, $node".assertionError

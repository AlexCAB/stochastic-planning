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
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.variable.IoVariable
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.db.Label
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.IoIndex
import planning.engine.map.database.Neo4jQueries.{IN_LABEL, IO_LABEL, OUT_LABEL}
import planning.engine.map.database.model.extensions.is

type ConcreteNodeMap[F[_]] = Map[IoIndex, List[ConcreteNode[F]]]

trait IoNode[F[_]: MonadThrow]:
  val name: Name
  val variable: IoVariable[F, ?]

  private lazy val thisLabel: F[Label] = this match
    case _: InputNode[F]  => IN_LABEL.pure
    case _: OutputNode[F] => OUT_LABEL.pure
    case _                => s"Unknown node type: $this".assertionError

  private lazy val thisParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.NAME -> name.value.toDbParam,
    PROP_NAME.VARIABLE -> variable.toQueryParams
  )

  protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]

//  private[map] def addConcreteNode(n: ConcreteNode[F]): F[ConcreteNode[F]] = hiddenNodes
//    .update:
//      case ns if ns.contains(n.valueIndex) => ns.updated(n.valueIndex, ns(n.valueIndex) :+ n)
//      case ns                              => ns.updated(n.valueIndex, List(n))
//    .as(n)

  private[map] def getAllConcreteNode: F[ConcreteNodeMap[F]] = hiddenNodes.get

  def toQueryParams: F[(Label, Map[String, Param])] =
    for
      label <- thisLabel
      params <- thisParams
    yield (label, params)

  override def equals(obj: Any): Boolean = (obj, this) match
    case (that: InputNode[?], self: InputNode[?])   => self.name == that.name && self.variable == that.variable
    case (that: OutputNode[?], self: OutputNode[?]) => self.name == that.name && self.variable == that.variable
    case _                                          => false

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"

object IoNode:
  def fromNode[F[_]: Concurrent](node: Node): F[IoNode[F]] = node match
    case n if n.is(IO_LABEL) =>
      for
        name <- node.properties.getValue[F, String](PROP_NAME.NAME).flatMap(Name.fromString)
        variable <- node.properties.getProps(PROP_NAME.VARIABLE).flatMap(IoVariable.fromProperties[F])
        ioNode <- node match
          case n if n.is(IN_LABEL)  => InputNode[F](name, variable).map(_.asInstanceOf[IoNode[F]])
          case n if n.is(OUT_LABEL) => OutputNode[F](name, variable).map(_.asInstanceOf[IoNode[F]])
          case n                    => s"Unknown node type, node: $n".assertionError
      yield ioNode
    case _ => s"Not a IO node: $node".assertionError

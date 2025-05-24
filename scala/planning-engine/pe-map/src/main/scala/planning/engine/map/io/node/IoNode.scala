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
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.map.database.Neo4jQueries.{IN_LABEL, IO_LABEL, OUT_LABEL}

type ConcreteNodeMap[F[_]] = Map[IoIndex, Map[HnId, ConcreteNode[F]]]

trait IoNode[F[_]: MonadThrow]:
  val name: Name
  val variable: IoVariable[F, ?]
  protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]

  private lazy val thisLabel: F[Label] = this match
    case _: InputNode[F]  => IN_LABEL.pure
    case _: OutputNode[F] => OUT_LABEL.pure
    case _                => s"Unknown node type: $this".assertionError

  private lazy val thisParams: F[Map[String, Param]] = paramsOf(
    PROP_NAME.NAME -> name.value.toDbParam,
    PROP_NAME.VARIABLE -> variable.toQueryParams
  )
  
  private[map] def getState: F[ConcreteNodeMap[F]] = hiddenNodes.get

  private[map] def addConcreteNode[R](n: ConcreteNode[F], block: => F[R]): F[(ConcreteNode[F], R)] =
    hiddenNodes.evalModify(state =>
      for
        newState <- state match
          case ns if ns.getOrElse(n.valueIndex, Map()).contains(n.id) => s"Node $n exists in: $state".assertionError
          case ns if ns.contains(n.valueIndex) => ns.updated(n.valueIndex, ns(n.valueIndex).updated(n.id, n)).pure
          case ns                              => ns.updated(n.valueIndex, Map(n.id -> n)).pure
        res <- block
      yield (newState, (n, res))
    )

  private[map] def removeConcreteNode[R](n: ConcreteNode[F], block: => F[R]): F[R] = hiddenNodes.evalModify(state =>
    for
      newState <- state.get(n.valueIndex) match
        case Some(nodes) if nodes.contains(n.id) =>
          nodes.removed(n.id) match
            case ns if ns.isEmpty => state.removed(n.valueIndex).pure
            case ns               => state.updated(n.valueIndex, ns).pure
        case _ => s"Node $n not found in: $state".assertionError
      res <- block
    yield (newState, res)
  )

  private[map] def getAllConcreteNode: F[ConcreteNodeMap[F]] = hiddenNodes.get

  private[map] def toQueryParams: F[(Label, Map[String, Param])] =
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
  private[map] def fromNode[F[_]: Concurrent](node: Node): F[IoNode[F]] = node match
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

  private[map] def nameFromNode[F[_]: MonadThrow](node: Node): F[Name] = node match
    case n if n.is(IO_LABEL) => node.properties.getValue[F, String](PROP_NAME.NAME).flatMap(Name.fromString)
    case _                   => s"Not a IO node: $node".assertionError

  private[map] def findForNode[F[_]: MonadThrow](node: Option[Node], ioNodes: List[IoNode[F]]): F[Option[IoNode[F]]] =
    node match
      case Some(n) =>
        for
          name <- nameFromNode(n)
          ioNode <- ioNodes.find(_.name == name) match
            case Some(ioNode) => ioNode.pure[F]
            case None         => s"Node with name ${name} not found, in list: $ioNodes".assertionError
        yield Some(ioNode)
      case _ => None.pure

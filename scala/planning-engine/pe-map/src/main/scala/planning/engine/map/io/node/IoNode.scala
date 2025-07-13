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
import planning.engine.map.io.variable.IoVariable
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.text.Name
import planning.engine.common.values.db.Neo4j.{IN_LABEL, IO_LABEL, Label, OUT_LABEL}

trait IoNode[F[_]: MonadThrow] extends Validation:
  val name: Name
  val variable: IoVariable[F, ?]

  private lazy val thisLabel: F[Label] = this match
    case _: InputNode[F]  => IN_LABEL.pure
    case _: OutputNode[F] => OUT_LABEL.pure
    case _                => s"Unknown node type: $this".assertionError

  private lazy val thisParams: F[Map[String, Param]] = paramsOf(
    PROP.NAME -> name.value.toDbParam,
    PROP.VARIABLE -> variable.toQueryParams
  )

  lazy val validationName = s"IoNode(type = $thisLabel, name = $name, variable = $variable)"
  lazy val validationErrors = validations(name.value.nonEmpty -> "Name must not be empty")

  def toQueryParams: F[(Label, Map[String, Param])] =
    for
      label <- thisLabel
      params <- thisParams
    yield (label, params)

  override def toString: String = s"${this.getClass.getSimpleName}(name = $name, variable = $variable)"

object IoNode:
  def fromNode[F[_]: Concurrent](node: Node): F[IoNode[F]] = node match
    case n if n.is(IO_LABEL) =>
      for
        name <- node.properties.getValue[F, String](PROP.NAME).flatMap(Name.fromString)
        variable <- node.properties.getProps(PROP.VARIABLE).flatMap(IoVariable.fromProperties[F])
        ioNode <- node match
          case n if n.is(IN_LABEL)  => InputNode[F](name, variable).asInstanceOf[IoNode[F]].pure
          case n if n.is(OUT_LABEL) => OutputNode[F](name, variable).asInstanceOf[IoNode[F]].pure
          case n                    => s"Unknown node type, node: $n".assertionError
      yield ioNode
    case _ => s"Not a IO node: $node".assertionError

  def nameFromNode[F[_]: MonadThrow](node: Node): F[Name] = node match
    case n if n.is(IO_LABEL) => node.properties.getValue[F, String](PROP.NAME).flatMap(Name.fromString)
    case _                   => s"Not a IO node: $node".assertionError

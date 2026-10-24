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
| created: 2026-10-24 |||||||||||*/

package planning.engine.yed

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.{Description, Name}

// Node of the yEd graph, parsed from its label. Label format: `<type>:<field>:...:<description>`, where type is:
//   G - graph:    `G:<name>:<description>`
//   S - sample:   `S:<name>:<probability count>:<utility>:<description>`
//   I/O - io:     `I:<name>:<bool|int|float|list>:<value range, separated by ';'>:<description>`
//   C - concrete: `C:<io variable name>:<value>:<name>:<description>`
//   A - abstract: `A:<name>:<description>`
enum ParsedNode:
  case Graph(id: String, name: Name, description: Option[Description])
  case Sample(id: String, name: Name, probabilityCount: Long, utility: Double, description: Option[Description])
  case Input(id: String, ioDef: IoNodeApiDef, description: Option[Description])
  case Output(id: String, ioDef: IoNodeApiDef, description: Option[Description])
  case Concrete(id: String, name: HnName, ioName: IoName, value: String, description: Option[Description])
  case Abstract(id: String, name: HnName, description: Option[Description])

  def id: String

object ParsedNode:
  // Label split on `:` fields, with helpers to read and validate them.
  private final class Label[F[_]: MonadThrow](val text: String):
    private val parts: List[String] = text.split(":", -1).toList

    def field(i: Int, fieldName: String): F[String] = parts.lift(i).map(_.trim).filter(_.nonEmpty) match
      case Some(value) => value.pure
      case None        => s"$fieldName is empty or not defined, label = $text".assertionError

    def number[N](i: Int, fieldName: String)(parse: String => N): F[N] = field(i, fieldName)
      .flatMap(str => MonadThrow[F].catchNonFatal(parse(str)))
      .adaptError(err => AssertionError(s"Invalid $fieldName, label = $text", err))

    // Description is the rest of the label (may contain `:`), empty description is treated as not defined.
    def description(from: Int): Option[Description] =
      Some(parts.drop(from).mkString(":")).filter(_.nonEmpty).map(Description.apply)

  private def sample[F[_]: MonadThrow](id: String, label: Label[F]): F[ParsedNode] =
    for
      name <- label.field(1, "Node name")
      probabilityCount <- label.number(2, "Probability count")(_.toLong)
      _ <- probabilityCount.assertPositive(s"Probability count should be > 0, label = ${label.text}")
      utility <- label.number(3, "Utility")(_.toDouble)
    yield Sample(id, Name(name), probabilityCount, utility, label.description(4))

  // Documented range format is `t;f`, full names are accepted as well.
  private def parseBool(str: String): Boolean = str.toLowerCase match
    case "t" | "true"  => true
    case "f" | "false" => false
    case _             => throw IllegalArgumentException(s"Invalid boolean value: $str, expected `t` or `f`")

  private def ioNodeDef[F[_]: MonadThrow](
      name: IoName,
      valueType: String,
      values: List[String],
      labelText: String,
  ): F[IoNodeApiDef] =
    def parseAll[N](parse: String => N): F[List[N]] = MonadThrow[F]
      .catchNonFatal(values.map(parse))
      .adaptError(err => AssertionError(s"Invalid value range: $values for type `$valueType`, label = $labelText", err))

    def minMax[N: Ordering](parse: String => N)(build: (N, N) => IoNodeApiDef): F[IoNodeApiDef] =
      for
        range <- parseAll(parse)
        _ <- (range.size == 2).assertTrue(s"Invalid value range: $values, expected 2 values (min, max), label = $labelText")
        _ <- Ordering[N].lt(range.head, range(1)).assertTrue(s"Min should be less than max, label = $labelText")
      yield build(range.head, range(1))

    valueType match
      case "bool" =>
        for
          range <- parseAll(parseBool)
          _ <- (range.nonEmpty && range.size <= 2)
            .assertTrue(s"Invalid value range: $values, for type `bool` expected 1 or 2 values, label = $labelText")
        yield BooleanIoNodeDef(name, range.toSet)

      case "int"   => minMax(_.toInt)(IntIoNodeDef(name, _, _))
      case "float" => minMax(_.toFloat)(FloatIoNodeDef(name, _, _))
      case "list" =>
        values.assertNonEmpty(s"List of values is empty, label = $labelText").as(ListStrIoNodeDef(name, values))

      case unknown =>
        s"Unknown value type: $unknown, expected one of: bool, int, float or list, label = $labelText".assertionError

  private def io[F[_]: MonadThrow](id: String, isInput: Boolean, label: Label[F]): F[ParsedNode] =
    for
      name <- label.field(1, "Node name")
      valueType <- label.field(2, "Value type")
      range <- label.field(3, "Value range")
      ioDef <- ioNodeDef(IoName(name), valueType, range.split(";", -1).toList.map(_.trim), label.text)
    yield if isInput then Input(id, ioDef, label.description(4)) else Output(id, ioDef, label.description(4))

  private def concrete[F[_]: MonadThrow](id: String, label: Label[F]): F[ParsedNode] =
    for
      ioName <- label.field(1, "Variable name")
      value <- label.field(2, "Value")
      name <- label.field(3, "Node name")
    yield Concrete(id, HnName(name), IoName(ioName), value, label.description(4))

  def fromLabel[F[_]: MonadThrow](id: String, labelText: String): F[ParsedNode] =
    val label = Label[F](labelText)

    for
      _ <- id.nonEmpty.assertTrue(s"Node ID is empty, label = $labelText")
      nodeType <- label.field(0, "Node type")
      node <- nodeType match
        case "G"       => label.field(1, "Node name").map(name => Graph(id, Name(name), label.description(2)))
        case "S"       => sample(id, label)
        case "I" | "O" => io(id, nodeType == "I", label)
        case "C"       => concrete(id, label)
        case "A"       => label.field(1, "Node name").map(name => Abstract(id, HnName(name), label.description(2)))
        case unknown   => s"Unknown node type: $unknown, expected one of: G, S, I, O, C, A, label = $labelText"
            .assertionError
    yield node

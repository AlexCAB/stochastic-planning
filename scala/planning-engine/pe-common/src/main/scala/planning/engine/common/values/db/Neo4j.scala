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
| created: 2025-05-15 |||||||||||*/

package planning.engine.common.values.db

object Neo4j:
  type Label = String

  val ROOT_LABEL: Label = "Root"
  val SAMPLES_LABEL: Label = "Samples"
  val SAMPLE_LABEL: Label = "Sample"
  val IO_NODES_LABEL: Label = "IoNodes"

  val IO_LABEL: Label = "Io"
  val IN_LABEL: Label = "In"
  val OUT_LABEL: Label = "Out"

  val HN_LABEL: Label = "Hn"
  val CONCRETE_LABEL: Label = "Concrete"
  val ABSTRACT_LABEL: Label = "Abstract"

  val LINK_LABEL: Label = "Link"
  val THEN_LABEL: Label = "Then"

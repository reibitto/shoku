package shoku

import enumeratum.*
import enumeratum.EnumEntry.LowerCamelcase

sealed trait ShokuDisplayMode extends LowerCamelcase

object ShokuDisplayMode extends Enum[ShokuDisplayMode] with CirceEnum[ShokuDisplayMode] {
  case object NoDistractions extends ShokuDisplayMode
  case object PlainWindow extends ShokuDisplayMode

  val values: IndexedSeq[ShokuDisplayMode] = findValues
}

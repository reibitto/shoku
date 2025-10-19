package shoku

sealed trait ShokuDisplayMode

object ShokuDisplayMode {
  case object NoDistractions extends ShokuDisplayMode
  case object PlainWindow    extends ShokuDisplayMode
}

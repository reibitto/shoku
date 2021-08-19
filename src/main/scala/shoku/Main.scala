package shoku

object Main {
  def main(args: Array[String]): Unit = {
    val window = ShokuWindow.make()
    window.setVisible(true)
  }
}

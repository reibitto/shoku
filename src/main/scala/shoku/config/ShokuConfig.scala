package shoku.config

import io.circe.{Decoder, Encoder}
import io.circe.syntax.*
import shoku.ShokuDisplayMode
import zio.*

import java.awt.Toolkit
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

final case class ShokuConfig(x: Int, y: Int, width: Int, height: Int, displayMode: ShokuDisplayMode)

object ShokuConfig {

  implicit val decoder: Decoder[ShokuConfig] =
    Decoder.forProduct5("x", "y", "width", "height", "displayMode")(ShokuConfig.apply)

  implicit val encoder: Encoder[ShokuConfig] =
    Encoder.forProduct5("x", "y", "width", "height", "displayMode")(a => (a.x, a.y, a.width, a.height, a.displayMode))

  def default: ShokuConfig =
    Try(Toolkit.getDefaultToolkit.getScreenSize).toOption match {
      case Some(screenSize) =>
        val targetWidth = screenSize.width / 2
        val targetHeight = screenSize.height / 4

        ShokuConfig(0, 0, targetWidth, targetHeight, ShokuDisplayMode.PlainWindow)

      case None => ShokuConfig(0, 0, 400, 200, ShokuDisplayMode.PlainWindow)
    }

  def defaultConfigPath: Path = Paths.get("shoku.json")

  def loadFromFile(path: Path): Task[ShokuConfig] =
    for {
      input  <- ZIO.attempt(Files.readString(path))
      json   <- ZIO.fromEither(io.circe.parser.parse(input))
      config <- ZIO.fromEither(json.as[ShokuConfig])
    } yield config

  def saveToFile(path: Path, config: ShokuConfig): Task[Unit] =
    ZIO.attempt(Files.writeString(path, config.asJson.spaces2))

}

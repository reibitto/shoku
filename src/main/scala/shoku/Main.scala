package shoku

import shoku.config.ShokuConfig
import zio.*

import java.nio.file.NoSuchFileException

object Main {

  def main(args: Array[String]): Unit =
    Unsafe.unsafe { implicit u =>
      Runtime.default.unsafe.run {
        for {
          config <- ShokuConfig
                      .loadFromFile(ShokuConfig.defaultConfigPath)
                      .catchAll {
                        case _: NoSuchFileException =>
                          ZIO.succeed(ShokuConfig.default)

                        case other =>
                          other.printStackTrace()

                          ZIO.succeed(ShokuConfig.default)

                      }
          _ <- ZIO.attempt {
                 val window = ShokuWindow.make(config)
                 window.setVisible(true)
               }
        } yield ()
      }.getOrThrowFiberFailure()
    }
}

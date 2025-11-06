package shoku

import shoku.config.ShokuConfig
import zio.*

import java.awt.{Color, FlowLayout, Toolkit}
import java.awt.event.*
import javax.swing.{JButton, JFrame, WindowConstants}

// TODO: Put all state into 1 atomic Ref
class ShokuWindow(var frame: JFrame, var noDistractionsButton: JButton, config: ShokuConfig) {

  def displayMode: ShokuDisplayMode =
    if (frame.isUndecorated) {
      ShokuDisplayMode.NoDistractions
    } else {
      ShokuDisplayMode.PlainWindow
    }

  private def recreateFrame(f: => Unit): Unit = {
    val oldFrame = frame
    oldFrame.dispose()

    val window = ShokuWindow.make(config)
    frame = window.frame
    noDistractionsButton = window.noDistractionsButton
    frame.setBounds(oldFrame.getX, oldFrame.getY, oldFrame.getWidth, oldFrame.getHeight)

    f

    setVisible(true)
  }

  def setDisplayMode(mode: ShokuDisplayMode, shouldRecreateFrame: Boolean = true): Unit = {
    def setMode(): Unit =
      mode match {
        case ShokuDisplayMode.PlainWindow =>
          frame.setUndecorated(false)
          noDistractionsButton.setVisible(true)

        case ShokuDisplayMode.NoDistractions =>
          frame.setUndecorated(true)
          noDistractionsButton.setVisible(false)
      }

    if (shouldRecreateFrame)
      recreateFrame {
        setMode()
      }
    else
      setMode()
  }

  def cycleDisplayMode(): Unit =
    displayMode match {
      case ShokuDisplayMode.PlainWindow =>
        setDisplayMode(ShokuDisplayMode.NoDistractions)

      case ShokuDisplayMode.NoDistractions =>
        setDisplayMode(ShokuDisplayMode.PlainWindow)
    }

  def setVisible(visible: Boolean): Unit =
    frame.setVisible(visible)

  def close(): Task[Unit] = {
    val bounds = frame.getBounds

    for {
      _ <- ZIO.succeed(frame.setVisible(false))
      _ <- ShokuConfig.saveToFile(
             ShokuConfig.defaultConfigPath,
             ShokuConfig(
               bounds.x,
               bounds.y,
               bounds.width,
               bounds.height,
               displayMode
             )
           )
      _ <- ZIO.succeed(frame.dispose())
    } yield ()
  }
}

object ShokuWindow {

  def make(config: ShokuConfig): ShokuWindow = {
    var pressedLocation: Option[(Int, Int)] = None
    val frame = new JFrame("蝕")

    frame.setBounds(config.x, config.y, config.width, config.height)
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.setAlwaysOnTop(true)

    val hideBorderButton = new JButton("Hide border")
    hideBorderButton.setFocusable(false)
    hideBorderButton.setToolTipText("Hide border (E)")

    val window = new ShokuWindow(frame, hideBorderButton, config)
    window.setDisplayMode(config.displayMode, shouldRecreateFrame = false)

    hideBorderButton.addActionListener { (_: ActionEvent) =>
      window.cycleDisplayMode()
    }

    frame.getContentPane.setLayout(new FlowLayout())
    frame.getContentPane.setBackground(Color.BLACK)
    frame.getContentPane.add(hideBorderButton)

    frame.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit =
        (e.getKeyCode, e.isShiftDown) match {
          case (KeyEvent.VK_Q, _) =>
            Unsafe.unsafe { implicit u =>
              Runtime.default.unsafe.run {
                window.close()
              }.getOrThrowFiberFailure()
            }

          case (KeyEvent.VK_E, _) =>
            window.cycleDisplayMode()

          case (KeyEvent.VK_H, _) =>
            frame.setState(java.awt.Frame.ICONIFIED)

          case (KeyEvent.VK_W, false) =>
            val location = window.frame.getLocation
            window.frame.setLocation(location.x, location.y - 10)

          case (KeyEvent.VK_A, false) =>
            val location = window.frame.getLocation
            window.frame.setLocation(location.x - 10, location.y)

          case (KeyEvent.VK_S, false) =>
            val location = window.frame.getLocation
            window.frame.setLocation(location.x, location.y + 10)

          case (KeyEvent.VK_D, false) =>
            val location = window.frame.getLocation
            window.frame.setLocation(location.x + 10, location.y)

          case (KeyEvent.VK_W, true) =>
            val size = window.frame.getSize
            window.frame.setSize(size.width, size.height - 10)

          case (KeyEvent.VK_A, true) =>
            val size = window.frame.getSize
            window.frame.setSize(size.width - 10, size.height)

          case (KeyEvent.VK_S, true) =>
            val size = window.frame.getSize
            window.frame.setSize(size.width, size.height + 10)

          case (KeyEvent.VK_D, true) =>
            val size = window.frame.getSize
            window.frame.setSize(size.width + 10, size.height)

          case (KeyEvent.VK_RIGHT, true) =>
            val screenSize = Toolkit.getDefaultToolkit.getScreenSize
            val windowSize = window.frame.getSize
            val targetWidth = screenSize.width - window.frame.getX
            window.frame.setSize(targetWidth, windowSize.height)

          case (KeyEvent.VK_DOWN, true) =>
            val screenSize = Toolkit.getDefaultToolkit.getScreenSize
            val windowSize = window.frame.getSize
            val targetHeight = screenSize.height - window.frame.getY
            window.frame.setSize(windowSize.width, targetHeight)

          case _ => ()

        }
    })

    frame.addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit =
        pressedLocation = Some((e.getX, e.getY))

      override def mouseReleased(e: MouseEvent): Unit =
        pressedLocation = None
    })

    frame.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseDragged(e: MouseEvent): Unit =
        pressedLocation.foreach { case (x, y) =>
          val dx = e.getX - x
          val dy = e.getY - y
          frame.setLocation(frame.getX + dx, frame.getY + dy)
        }
    })

    frame.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit =
        Unsafe.unsafe { implicit u =>
          Runtime.default.unsafe.run {
            window.close()
          }.getOrThrowFiberFailure()
        }
    })

    window
  }
}

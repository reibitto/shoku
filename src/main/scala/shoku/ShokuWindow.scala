package shoku

import java.awt.event._
import java.awt.{ Color, FlowLayout }
import javax.swing.{ JButton, JFrame, WindowConstants }

// TODO: Put all state into 1 atomic Ref
class ShokuWindow(var frame: JFrame, var noDistractionsButton: JButton) {
  def displayMode: ShokuDisplayMode =
    if (frame.isUndecorated) {
      ShokuDisplayMode.NoDistractions
    } else {
      ShokuDisplayMode.PlainWindow
    }

  private def recreateFrame(f: => Unit): Unit = {
    val oldFrame = frame
    oldFrame.dispose()

    val window = ShokuWindow.make()
    frame = window.frame
    noDistractionsButton = window.noDistractionsButton
    frame.setLocation(oldFrame.getX, oldFrame.getY)
    frame.setSize(oldFrame.getWidth, oldFrame.getHeight)

    f

    setVisible(true)
  }

  def setDisplayMode(mode: ShokuDisplayMode): Unit =
    recreateFrame {
      mode match {
        case ShokuDisplayMode.PlainWindow =>
          frame.setUndecorated(false)
          noDistractionsButton.setVisible(true)

        case ShokuDisplayMode.NoDistractions =>
          frame.setUndecorated(true)
          noDistractionsButton.setVisible(false)
      }
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
}

object ShokuWindow {
  def make(width: Int = 300, height: Int = 200): ShokuWindow = {
    var pressedLocation: Option[(Int, Int)] = None
    val frame                               = new JFrame("蝕")

    frame.setSize(width, height)
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

    val hideBorderButton = new JButton("Hide border")
    hideBorderButton.setBackground(Color.BLACK)
    hideBorderButton.setForeground(Color.WHITE)
    hideBorderButton.setFocusable(false)
    hideBorderButton.setToolTipText("Hide border (Q)")

    val window = new ShokuWindow(frame, hideBorderButton)

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
            window.cycleDisplayMode()

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

    window
  }
}

package chatsimple

import retier._
import retier.architectures.MultiClientServer._
import retier.rescalaTransmitter._
import retier.serializable.upickle._
import retier.tcp._

import rescala._

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat


@multitier
object Chat {
  trait Server extends ServerPeer[Client]
  trait Client extends ClientPeer[Server]

  val message = placed[Client] { Evt[String] }

  val publicMessage = placed[Server].issued { client: Remote[Client] =>
    message.asLocalSeq collect {
      case (remote, message) if remote != client => message
    }
  }

  placed[Client].main {
    publicMessage.asLocal observe println

    for (line <- io.Source.stdin.getLines)
      message fire line
  }
}

object Server extends App {
  multitier setup new Chat.Server {
    def connect = setup (TCP) ("tcp://43053")
  }
}

object Client extends App {
  multitier setup new Chat.Client {
    def connect = setup (TCP) ("tcp://localhost:43053")
  }
}

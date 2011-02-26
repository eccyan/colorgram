import java.net._
import java.io._
import java.awt._
import javax.imageio._

import scala.actors._
import scala.actors.Actor._

case class Echo(socket: Socket)

object Service extends Actor {
    implicit def inputStreamWrapper(in: InputStream) =
	new BufferedReader(new InputStreamReader(in))
    implicit def outputStreamWrapper(out: OutputStream) =
	new PrintWriter(new OutputStreamWriter(out))

def avarage
    def echo(in: BufferedReader, out: PrintWriter) {
	val url = new URL( in.readLine() )
	val image  = ImageIO.read(url);
	for (i <- 0 to image.getWidth; j <- 0 to image.getHeight) 
		image.getRGB(i, j)  
    }

    def act() {
	loop {
	    receive {
		case Echo(socket) =>
		    actor {
			echo(socket.getInputStream(), socket.getOutputStream())
			socket.close
		    }
	    }
	}
    }
}

object EchoServer {
    Service.start

    val serverSocket = new ServerSocket(12111)

    def start() {
	while(true) {
	    println("about to block")
	    val socket = serverSocket.accept()
	    Service ! Echo(socket)
	    println("back from actor")
	}
    }
}

EchoServer.start

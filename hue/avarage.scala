import java.net._
import java.io._

import scala.actors._
import scala.collection._
import scala.actors.Actor._

case class CSV(socket: Socket)

object Averager extends Actor {
    implicit def inputStreamWrapper(in: InputStream) =
	new BufferedReader(new InputStreamReader(in))
    implicit def outputStreamWrapper(out: OutputStream) =
	new PrintWriter(new OutputStreamWriter(out))

    def average(in: BufferedReader, out: PrintWriter) {
	val line = in readLine
	val numbers = line.split("[,\n]") filter ( number => number.matches("[0-9]*") )  map (number => Integer.parseInt(number) )
	out.println( (0 /: numbers){ _ + _ }.asInstanceOf[Float] / numbers.length)
	out.flush
    }

    def act() {
	loop {
	    receive {
		case CSV(socket) =>
		    actor {
			average(socket.getInputStream(), socket.getOutputStream())
			socket.close
		    }
	    }
	}
    }
}

object EchoServer {
    Averager.start

    val serverSocket = new ServerSocket(12111)

    def start() {
	while(true) {
	    println("about to block")
	    val socket = serverSocket.accept()
	    Averager ! CSV(socket)
	    println("back from actor")
	}
    }
}

EchoServer.start

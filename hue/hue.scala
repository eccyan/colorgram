import java.net._
import java.io._
import javax.imageio._

import scala.actors._
import scala.actors.Actor._

case class PhotoURL(socket: Socket)

object HueGetter extends Actor {
    implicit def inputStreamWrapper(in: InputStream) =
	new BufferedReader(new InputStreamReader(in))
    implicit def outputStreamWrapper(out: OutputStream) =
	new PrintWriter(new OutputStreamWriter(out))

    def hue(in: BufferedReader, out: PrintWriter) {
	val url = new URL( in readLine() )
	val image  = ImageIO read(url)
	val pixcels = for (i <- 0 to image.getWidth - 1; j <- 0 to image.getHeight - 1) yield image.getRGB(i, j)

	val r = pixcels map(pixcel => (pixcel >>> 0x10) & 0xff)
	val g = pixcels map(pixcel => (pixcel >>> 0x08) & 0xff)
	val b = pixcels map(pixcel => (pixcel >>> 0x00) & 0xff)

	val count = image.getWidth * image.getHeight 
	val avgR = (0 /: r) { _ + _ } / count
	val avgG = (0 /: g) { _ + _ } / count
	val avgB = (0 /: b) { _ + _ } / count

	out.println( (avgR << 0x10) | (avgG << 0x08) | (avgB << 0x00) )
	out.flush
    }

    def act() {
	loop {
	    receive {
		case PhotoURL(socket) =>
		    actor {
			hue(socket.getInputStream(), socket.getOutputStream())
			socket.close
		    }
	    }
	}
    }
}

object Server {
    HueGetter start

    val serverSocket = new ServerSocket(12111)

    def start() {
	while(true) {
	    println("about to block")
	    val socket = serverSocket.accept()
	    HueGetter ! PhotoURL(socket)
	    println("back from actor")
	}
    }
}

Server start

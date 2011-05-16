package colorgram.crowler

import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import scala.actors._
import scala.actors.Actor._
import graphics._
import parser._

/**
 * Main
 */
object Crowler {
    def main(args : Array[String]) = {
	println("start crowler")

	val pid :PID = "0"
	while (true) {
	    val url = new URL("http://instagr.am/p/" + (pid++) + "/")
	    try {
		val parser = new InstagramParser(url, 1000);
		val attribute = new ColorAttribute( ImageIO read(parser getPhotoURL) )

		print(url)
		print(", "+ (attribute getHue) )
		print(", "+ (attribute getSaturation) )
		print(", "+ (attribute getBrightness) )
		println()
	    }
	    catch {
	    	case e: IOException => println("could not connect url.")
	    }
	}

	println("end crowler")
    }
}

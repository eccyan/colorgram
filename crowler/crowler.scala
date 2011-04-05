import java.io.IOException
import java.net.URL
import java.awt.{ Color, Image }
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import scala.actors._
import scala.actors.Actor._

import org.jsoup._

/**
 * Class for Instagram parmanent link ID 
 */
class PID(initPID: String) {
    require( !(initPID isEmpty) )

    private val characters = Vector(
	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    );

    private var pid = initPID

    private def carry(pid: String): String = {
    	def loop(pid:String, carried: Boolean, result:String): String = {
	    // If pid is empty then quit loop
	    if (pid.isEmpty) return characters(0) + result
	    // If not carried then quit loop
	    if (!carried) return pid + result

	    val reversed = pid reverse
	    val index = characters indexOf( reversed head )
	    // Get character on carry up
	    val c = if (index + 1 < characters.size) characters(index+1) else characters(0)
	    // Update character 
	    val updated = reversed updated(0, c)

	    loop(updated drop(1) reverse, ( reversed.head == characters.last ), updated.take(1) + result)
	}
	loop(pid, true, "") 
    }

    def ++(): PID = {
        pid = carry(pid)
    	this
    }

    override def toString(): String = pid
}

/**
 * Object from PID class
 */
object PID {
    implicit def stringToPID(s: String) = new PID(s) 
}

/**
 * Class for parser instagram page
 */
class InstagramParser(url: URL, timeout: Int) {
    // Get document from URL
    private val document = { 
	// Only for instagr.am
    	if (url.getHost != "instagr.am") throw new IllegalArgumentException("host name is not instagr.am"); 
    	Jsoup parse(url, timeout)
    }

    // Get ID from profile photo url with regex 
    def getID() = {
	val src = document select(".profile-photo") attr("src") 
	val tmp = """profile_[0-9]+""".r.findFirstIn(src)
	if (tmp != None) """profile_""".r replaceAllIn(tmp get, "") else ""
	//"""\..+$""".r replaceAllIn("""[0-9]+\..+$""".r.findFirstIn(src) get, "")
    }
    def getPhotoURL() = {
    	new URL( document select(".photo") attr("src") )
    }
}

/**
 * Class for color attributes
 */
class ColorAttribute(image:Image) {
    // Get HSB array of avarage photo
    private val hsb = {
    	// Scale down image is avarage color for photo
    	val scaled = image getScaledInstance(1, 1, Image.SCALE_AREA_AVERAGING)

	// Create buffer, and draw scaled image
	val buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) 
	buffer createGraphics() drawImage(scaled, 0, 0, Color.BLACK, null)

	// Get avrage color from buffer
    	val avarage = new Color( buffer getRGB(0, 0) )
	Color RGBtoHSB(avarage getRed, avarage getGreen, avarage getBlue, null)
    }

    def getHue() = hsb(0)
    def getSaturation() = hsb(1);
    def getBrightness() = hsb(2);
}

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

Crowler.main(Array(""))

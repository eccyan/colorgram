import java.net._
import java.io._
import javax.imageio._

import scala.actors._
import scala.actors.Actor._

import org.jsoup._

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
	    // if pid is empty then quit loop
	    if (pid.isEmpty) return characters(0) + result
	    // if not carried then quit loop
	    if (!carried) return pid + result

	    val reversed = pid reverse
	    val index = characters indexOf( reversed head )
	    // get character on carry up
	    val c = if (index + 1 < characters.size) characters(index+1) else characters(0)
	    // update character 
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

object PID {
    implicit def stringToPID(s: String) = new PID(s) 
}

class InstagramParser(url: URL, timeout: Int) {
    // get document from URL
    private val document = { 
	// only for instagr.am
    	if (url.getHost != "instagr.am") throw new IllegalArgumentException("host name is not instagr.am"); 
    	Jsoup parse(url, timeout)
    }

    // get ID from profile photo url with regex 
    def getID() = {
	val src = document select(".profile-photo") attr("src") 
	val tmp = """profile_[0-9]+""".r.findFirstIn(src)
	if (tmp != None) """profile_""".r replaceAllIn(tmp get, "") else ""
	//"""\..+$""".r replaceAllIn("""[0-9]+\..+$""".r.findFirstIn(src) get, "")
    }
    def getPhotoURL() = {
    	document select(".photo") attr("src")
    }
}

object Crowler {
    def main(args : Array[String]) = {
	println("start crowler")

	val pid :PID = "0"
	while (true) {
	    val url = new URL("http://instagr.am/p/" + (pid++) + "/")
	    try {
		val parser = new InstagramParser(url, 1000);

		println(url toString)
		println(parser getID)
		println(parser getPhotoURL)
	    }
	    catch {
	    	case e: IOException => println("could not connect url.")
	    }

	    Thread.sleep(1000)
	}

	println("end crowler")
    }
}

//Crowler.main(Array(""))

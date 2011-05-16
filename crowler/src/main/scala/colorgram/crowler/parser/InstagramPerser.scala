package colorgram.crowler.parser

import java.io.IOException
import java.net.URL
import org.jsoup._

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

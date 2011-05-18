package colorgram.crowler

import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import scala.actors._
import scala.actors.Actor._
import graphics._
import parser._

case class Start(pid: PID)

/**
 * Crowler
 */
class Crowler extends Actor {
    def act() = {
        receive {
            case Start(pid) => {
                val url = new URL("http://instagr.am/p/" + pid + "/")
                try {
                    val parser = new InstagramParser(url, 1000);
                    val attribute = new ColorAttribute( ImageIO read(parser getPhotoURL) )

                    print(url)
                    print(" "+ (attribute getHue) )
                    print(", "+ (attribute getSaturation) )
                    print(", "+ (attribute getBrightness) )
                    println()
                }
                catch {
                    case e: IOException => println(url + " could not connect")
                }
            }
        }
    }
}

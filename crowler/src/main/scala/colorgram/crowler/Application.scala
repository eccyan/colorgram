package colorgram.crowler

import instagram._

/**
 * Application
 */
object Application {
    def main(args: Array[String]) = {
        println("start crowler")

        val pid: ParmanentID = "0"
        while (true) {
            val crowler = new Crowler
            crowler.start
            crowler ! Start(pid)
            Thread.sleep(500)
            pid++
        }

        println("end crowler")
    }
}

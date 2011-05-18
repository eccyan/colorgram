package colorgram.crowler

/**
 * Execute Crowl
 */
object Executer {
    def main(args: Array[String]) = {
        println("start crowler")

        val pid: PID = "0"
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

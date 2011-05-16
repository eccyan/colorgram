package colorgram.crowler

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


package colorgram.crowler.graphics

import java.awt.{ Color, Image }
import java.awt.image.BufferedImage

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

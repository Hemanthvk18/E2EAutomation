package utilities;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SikuliUtility {
    private static final Logger logger = LoggerFactory.getLogger(SikuliUtility.class);

    public static boolean verifyImage(String imagePath, String imageName) throws IOException {

        // Load the image to get its dimensions
        BufferedImage img = ImageIO.read(new File(imagePath));
        System.out.println(
                "Pattern Size : "
                        + img.getWidth()
                        + " x "
                        + img.getHeight());

        Screen screen = new Screen();
        Pattern pattern = new Pattern(imagePath).similar(0.50f);
        Match match = screen.exists(pattern, 10);

        if (match != null) {
            logger.info("Image found: " + imageName);
            logger.info("Found at : {},{}", match.getX(), match.getY());
            return true;
        } else {
            logger.warn("Image not found: " + imageName);
            return false;
        }
    }


    public static boolean verifyImage(WebElement imageElement,
                                      String imagePath,
                                      String imageName) {

        try {
            // Load the image to get its dimensions
            BufferedImage img = ImageIO.read(new File(imagePath));
            System.out.println(
                    "Pattern Size : "
                            + img.getWidth()
                            + " x "
                            + img.getHeight());

            // Get Selenium element location
            Point location = imageElement.getLocation();

            // Get Selenium element size
            Dimension size = imageElement.getSize();

            // Create Sikuli Region
            Region region = new Region(
                    location.getX(),
                    location.getY(),
                    size.getWidth(),
                    size.getHeight());

//            Region region = new Region(
//                            location.getX() - 100,
//                            location.getY() - 100,
//                            size.getWidth() + 200,
//                            size.getHeight() + 200);

            System.out.println("Region Location : " + region.getX() + " , " + region.getY());
            System.out.println("Region Size : " + region.getW() + " x " + region.getH());

            // Create Pattern with similarity
            Pattern pattern = new Pattern(imagePath).similar(0.50f);

            // Search inside the region
            Match match = region.exists(pattern, 10);

            if (match != null) {
                System.out.println("Image Found : " + imageName);
                System.out.println("Match Score : " + match.getScore());
                return true;
            }

            System.out.println("Image Not Found : " + imageName);
            return false;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
}

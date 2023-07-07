package run.wyatt.oneplatform.common.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * @author Wyatt
 * @date 2023/6/15 10:31
 */
public class ImageUtil {
    public static String bufferedImageToBase64(BufferedImage image, String imageType) {
        if (imageType == null || imageType.isEmpty()) {
            imageType = "png";
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, imageType, stream);
            return "data:image/" + imageType + ";base64," + Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

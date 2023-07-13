package space.itoncek;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, DimensionsException {
        BufferedImage a = ImageIO.read(new File("./a.jpg"));
        BufferedImage b = ImageIO.read(new File("./b.jpg"));

        long total = 0;
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            screen(a,b);
            total += System.currentTimeMillis()-start;
        }

        System.out.println(total/100 + " ms");
    }

    public static BufferedImage screen(BufferedImage a, BufferedImage b) throws DimensionsException {
        if(a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            throw new DimensionsException();
        }

        BufferedImage out = new BufferedImage(a.getWidth(),a.getHeight(),a.getType());
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                Color ac = new Color(a.getRGB(x,y));
                Color bc = new Color(b.getRGB(x,y));
                float rr = 1 - (1 - ac.getRed()* (0.003921569f))*(1 - bc.getRed()* (0.003921569f));
                float gg = 1 - (1 - ac.getGreen()* (0.003921569f))*(1 - bc.getGreen()* (0.003921569f));
                float bb = 1 - (1 - ac.getBlue()* (0.003921569f))*(1 - bc.getBlue()* (0.003921569f));
                out.setRGB(x,y,new Color(rr,gg,bb,1.0f).getRGB());
            }
        }

        return out;
    }

    private static class DimensionsException extends Throwable {
    }
}
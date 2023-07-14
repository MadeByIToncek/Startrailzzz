package space.itoncek;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Start");
        long total = System.currentTimeMillis();
        File[] list = new File("C:\\Users\\plane\\Downloads\\zasilka-LPAZ62I2XSCBVDY9\\").listFiles();
        assert list != null;
        List<File> files = List.of(list);
        List<BufferedImage> imgs = new ArrayList<>();

        for (File file : files) {
            imgs.add(ImageIO.read(file));
        }

        while (imgs.size() > 1) {
            imgs = pairup(imgs);
        }
        ImageIO.write(imgs.get(0), "jpg", new File("./out.jpg"));

        System.out.println((System.currentTimeMillis() - total)/1000f + " s");
        System.out.println(1/((System.currentTimeMillis() - total)/files.size()/1000f) + " img/s");
    }

    public static List<BufferedImage> pairup(List<BufferedImage> input) {
        List<CompletableFuture<BufferedImage>> futures = new ArrayList<>();
        List<BufferedImage> success = new ArrayList<>();
        BufferedImage[] ins = input.toArray(new BufferedImage[0]);
        for (int i = 0; i < input.size()-1; i+=2) {
            int finalI = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return lighten(ins[finalI],ins[finalI +1]);
                } catch (DimensionsException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((img, e) -> success.add(img)));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return success;
    }

    /**
     * @param a First image to screen
     * @param b Second image to screen
     * @return Final image
     * @throws DimensionsException Dimensions are not the same
     */
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
    public static BufferedImage lighten(BufferedImage a, BufferedImage b) throws DimensionsException {
        if(a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            throw new DimensionsException();
        }

        BufferedImage out = new BufferedImage(a.getWidth(),a.getHeight(),a.getType());
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                Color ac = new Color(a.getRGB(x,y));
                Color bc = new Color(b.getRGB(x,y));
                float rr = Math.max(ac.getRed(), bc.getRed())/255f;
                float gg = Math.max(ac.getGreen(), bc.getGreen())/255f;
                float bb = Math.max(ac.getBlue(), bc.getBlue())/255f;
                out.setRGB(x,y,new Color(rr,gg,bb,1.0f).getRGB());
            }
        }

        return out;
    }

    private static class DimensionsException extends Throwable {
    }
}
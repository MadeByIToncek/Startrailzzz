package space.itoncek;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    private static final int THREADS = 32;
    public static List<BufferedImage> images = new ArrayList<>();
    public static ProgressBar bar;
    public static void main(String[] args) throws IOException, InterruptedException, DimensionsException {
        System.out.println("Start");
//        long total = System.currentTimeMillis();
        File[] list = new File("D:\\#astro\\2023\\08\\13 pers").listFiles();
        assert list != null;
        BufferedImage fin;
        List<Thread> threads = new ArrayList<>();

        bar = new ProgressBarBuilder()
                .setInitialMax(list.length)
                .setTaskName("Processing images")
                .setUnit(" imgs",1)
                .setSpeedUnit(ChronoUnit.SECONDS)
                .showSpeed()
                .setMaxRenderedLength(200)
                .build();
        for (int i = 0; i < THREADS; i++) {
            Thread t = getThread(list, i);
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        bar.close();

        bar = new ProgressBarBuilder()
                .setInitialMax(THREADS)
                .setTaskName("Merging threads")
                .setUnit(" imgs",1)
                .setSpeedUnit(ChronoUnit.SECONDS)
                .showSpeed()
                .setMaxRenderedLength(200)
                .build();

        fin = images.get(0);
        for (BufferedImage image : images) {
            fin = lighten(fin, image);
            bar.step();
        }

        bar.close();

        ImageIO.write(fin, "jpg", new File("./out.jpg"));

        //System.out.println((System.currentTimeMillis() - total) / 1000f + " s");
        //System.out.println(1 / ((System.currentTimeMillis() - total) / list.length / 1000f) + " img/s");
    }

    private static Thread getThread(File[] list, int i) {
        int len = list.length / THREADS;
        int stindx = len * i;
        int endidx = (len * (i + 1)) - 1;
        return new Thread(() -> {
            try {
                List<File> files = new ArrayList<>(Arrays.asList(list).subList(stindx, endidx + 1));
                BufferedImage finl = ImageIO.read(files.get(0));

                for (File file : files) {
                    finl = lighten(finl, ImageIO.read(file));
                    bar.step();
                }
                images.add(finl);
            } catch (IOException | DimensionsException e) {
                throw new RuntimeException(e);
            }

        });
    }

    /**
     * @param a First image to screen
     * @param b Second image to screen
     * @return Final image
     * @throws DimensionsException Dimensions are not the same
     */
    public static BufferedImage screen(BufferedImage a, BufferedImage b) throws DimensionsException {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            throw new DimensionsException();
        }

        BufferedImage out = new BufferedImage(a.getWidth(), a.getHeight(), a.getType());
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                Color ac = new Color(a.getRGB(x, y));
                Color bc = new Color(b.getRGB(x, y));
                float rr = 1 - (1 - ac.getRed() * (0.003921569f)) * (1 - bc.getRed() * (0.003921569f));
                float gg = 1 - (1 - ac.getGreen() * (0.003921569f)) * (1 - bc.getGreen() * (0.003921569f));
                float bb = 1 - (1 - ac.getBlue() * (0.003921569f)) * (1 - bc.getBlue() * (0.003921569f));
                out.setRGB(x, y, new Color(rr, gg, bb, 1.0f).getRGB());
            }
        }

        return out;
    }

    public static BufferedImage lighten(BufferedImage a, BufferedImage b) throws DimensionsException {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            throw new DimensionsException();
        }

        BufferedImage out = new BufferedImage(a.getWidth(), a.getHeight(), a.getType());
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                Color ac = new Color(a.getRGB(x, y));
                Color bc = new Color(b.getRGB(x, y));
                float rr = Math.max(ac.getRed(), bc.getRed()) / 255f;
                float gg = Math.max(ac.getGreen(), bc.getGreen()) / 255f;
                float bb = Math.max(ac.getBlue(), bc.getBlue()) / 255f;
                out.setRGB(x, y, new Color(rr, gg, bb, 1.0f).getRGB());
            }
        }

        return out;
    }

    private static class DimensionsException extends Throwable {
    }
}
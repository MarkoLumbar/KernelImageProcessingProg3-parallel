import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImgProcessor {

    public static BufferedImage convolution (BufferedImage image, double[][] kernel){
        System.out.println("Parallel Convolution");
        long start = System.nanoTime(); //zacetek merjenja

        int widthColumns = image.getWidth();
        int heightRows = image.getHeight();
        int imageType = image.getType();
        int kernelWidthColumns = kernel.length;
        int kernelHeightRows = kernel[0].length;
        int kernelOriginX = kernelWidthColumns / 2; //center kernela po x osi
        int kernelOriginY = kernelHeightRows / 2;

        if (imageType == 0) { //falback za unkown image type
            imageType = BufferedImage.TYPE_INT_RGB;
        }

        BufferedImage newImage = new BufferedImage(widthColumns, heightRows, imageType);

        //st threadov ampak najmanj 1, eden ostane za sistem
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        System.out.println("Number of threads: " + numThreads);


        int rowsToProcess = heightRows - 2 * kernelOriginY; //zaradi robov zgoraj in spodaj
        int rowsPerThread = rowsToProcess / numThreads;
        int remainingRows = rowsToProcess % numThreads;

        //thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        //seznam taskov
        List<Callable<Void>> tasks = new ArrayList<>();

        //zacetna pozicija rezanja slike prva veljavna vrstica
        int currentY = kernelOriginY;

        // vsak thread dobi svoj task ki obdela določen pas vrstic
        for (int i = 0; i < numThreads; i++) {
            int startY = currentY; //zacetek dolocene niti
            int rowsForThisThread = rowsPerThread + (i == numThreads - 1 ? remainingRows : 0); // koliko vrstic bo dolocena nit obdelala + zadnja nit dobi ostanek
            int endY = startY + rowsForThisThread; //koncna vrstica za doloceno nit - ni vključena v obdelavo


            Callable<Void> task = () -> {
                for (int y = startY; y <endY ; y++) {
                    for (int x = kernelOriginX; x <widthColumns - kernelOriginX ; x++) {
                        double redAcc = 0, greenAcc = 0, blueAcc = 0;

                        //loopamo kernel
                        for (int ky = 0; ky < kernelHeightRows; ky++) {
                            for (int kx = 0; kx < kernelWidthColumns; kx++) {
                                int imageX = x + kx - kernelOriginX;
                                int imageY = y + ky - kernelOriginY;

                                //http://tech.abdulfatir.com/2014/05/kernel-image-processing.html for extracting RGB and truncating values
                                int rgb = image.getRGB(imageX, imageY);
                                int red = (rgb >> 16) & 0xFF;
                                int green = (rgb >> 8) & 0xFF;
                                int blue = (rgb & 0xFF);

                                redAcc += red * kernel[ky][kx];
                                greenAcc += green * kernel[ky][kx];
                                blueAcc += blue * kernel[ky][kx];

                            }
                        }
                        int truncatedRed, truncatedGreen, truncatedBlue; //ker values so lahko samo od 0-255
                        truncatedRed = Math.max(0, Math.min(255, (int) redAcc));
                        truncatedGreen = Math.max(0, Math.min(255, (int) greenAcc));
                        truncatedBlue = Math.max(0, Math.min(255, (int) blueAcc));

                        Color color = new Color(truncatedRed, truncatedGreen, truncatedBlue);
                        newImage.setRGB(x, y, color.getRGB()); //nastavimo barvo pixla v novi sliki
                    }
                }
                return null;
            };

            tasks.add(task);
            currentY = endY; //premaknemo kazalec, da kaze na vrstico, kjer se zacne delo naslednje niti
        }

        try {
            executor.invokeAll(tasks); // start tasks in počakaj da končajo
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); //ugasni executor
        }

        long end = System.nanoTime();
        System.out.println("Execution time (ms): " + (end - start) / 1_000_000);

        return newImage;
    }
}

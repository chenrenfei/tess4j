/**
 * Copyright @ 2008 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.*;
import javax.imageio.IIOImage;

public class ImageHelper {

    /**
     * Convenience method that returns a scaled instance of the provided
     * {@code BufferedImage}.
     *
     * @param image the original image to be scaled
     * @param targetWidth the desired width of the scaled instance, in pixels
     * @param targetHeight the desired height of the scaled instance, in pixels
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage image, int targetWidth, int targetHeight) {
        int type = (image.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return tmp;
    }

    /**
     * Convenience method that returns a scaled instance of the provided
     * {@code IIOImage}.
     *
     * @param iioSource the original image to be scaled
     * @param scale the desired scale
     * @return a scaled version of the original {@code IIOImage}
     */
    public static IIOImage getScaledInstance(IIOImage iioSource, float scale) {
        if (!(iioSource.getRenderedImage() instanceof BufferedImage)) {
            throw new IllegalArgumentException("RenderedImage in IIOImage must be BufferedImage");
        }

        if (Math.abs(scale - 1.0) < 0.001) {
            return iioSource;
        }

        BufferedImage source = (BufferedImage) iioSource.getRenderedImage();
        BufferedImage target = getScaledInstance(source, (int) (scale * source.getWidth()), (int) (scale * source.getHeight()));
        return new IIOImage(target, null, null);
    }

    /**
     * A replacement for the standard <code>BufferedImage.getSubimage</code>
     * method.
     *
     * @param image
     * @param x the X coordinate of the upper-left corner of the specified
     * rectangular region
     * @param y the Y coordinate of the upper-left corner of the specified
     * rectangular region
     * @param width the width of the specified rectangular region
     * @param height the height of the specified rectangular region
     * @return a BufferedImage that is the subimage of <code>image</code>.
     */
    public static BufferedImage getSubImage(BufferedImage image, int x, int y, int width, int height) {
        int type = (image.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage tmp = new BufferedImage(width, height, type);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image.getSubimage(x, y, width, height), 0, 0, null);
        g2.dispose();
        return tmp;
    }

    /**
     * A simple method to convert an image to binary or B/W image.
     *
     * @param image input image
     * @return a monochrome image
     */
    public static BufferedImage convertImageToBinary(BufferedImage image) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return tmp;
    }
    
    /**
     * Removes alpha channel from image
     * 
     * @param image
     * @return 
     */
    public static BufferedImage removeAlphaChannel(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return image;
        }
        
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return tmp;
    }

    /**
     * A simple method to convert an image to binary or B/W image.
     *
     * @param image input image
     * @return a monochrome image
     * @deprecated As of release 1.1, renamed to
     * {@link #convertImageToBinary(BufferedImage image)}
     */
    @Deprecated
    public static BufferedImage convertImage2Binary(BufferedImage image) {
        return convertImageToBinary(image);
    }

    /**
     * A simple method to convert an image to gray scale.
     *
     * @param image input image
     * @return a monochrome image
     */
    public static BufferedImage convertImageToGrayscale(BufferedImage image) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return tmp;
    }

    private static final short[] invertTable;

    static {
        invertTable = new short[256];
        for (int i = 0; i < 256; i++) {
            invertTable[i] = (short) (255 - i);
        }
    }

    /**
     * Inverts image color.
     *
     * @param image input image
     * @return an inverted-color image
     */
    public static BufferedImage invertImageColor(BufferedImage image) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
        return invertOp.filter(image, tmp);
    }

    /**
     * Rotates an image.
     *
     * @param image the original image
     * @param angle the degree of rotation
     * @return a rotated image
     */
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        double theta = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(theta));
        double cos = Math.abs(Math.cos(theta));
        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage tmp = new BufferedImage(newW, newH, image.getType());
        Graphics2D g2d = tmp.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.translate((newW - w) / 2, (newH - h) / 2);
        g2d.rotate(theta, w / 2, h / 2);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return tmp;
    }

    /**
     * Gets an image from Clipboard.
     *
     * @return image
     */
    public static Image getClipboardImage() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return (Image) clipboard.getData(DataFlavor.imageFlavor);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clones an image.
     * http://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
     *
     * @param bi
     * @return
     */
    public static BufferedImage cloneImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}

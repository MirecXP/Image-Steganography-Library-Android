package com.ayush.imagesteganographylibrary.Text;

import android.graphics.Bitmap;

import com.ayush.imagesteganographylibrary.core.PixelSteganography;

/**
 * Bitmap facade over {@link PixelSteganography}, which holds the actual 2-bit LSB codec.
 */
class EncodeDecode {

    private EncodeDecode() {
    }

    /**
     * Hides an already encrypted message in the image.
     *
     * @return a new bitmap carrying the message, keeping the density of the original
     */
    static Bitmap encodeMessage(Bitmap source, String encryptedMessage, ProgressHandler progressHandler) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        if (progressHandler != null) {
            progressHandler.setTotal(PixelSteganography.messageByteCount(encryptedMessage));
        }
        int[] encoded = PixelSteganography.encode(pixels, width, height, encryptedMessage, encodedBytes -> {
            if (progressHandler != null) {
                progressHandler.increment(1);
            }
        });
        if (progressHandler != null) {
            progressHandler.finished();
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setDensity(source.getDensity());
        result.setPixels(encoded, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * @return the encrypted message hidden in the image, empty when there is none
     */
    static String decodeMessage(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        return PixelSteganography.decode(pixels, width, height);
    }

    /**
     * Calculate the numbers of pixel needed
     *
     * @return : The number of pixel {integer}
     * @parameter : message {Message to encode}
     */
    public static int numberOfPixelForMessage(String message) {
        return PixelSteganography.requiredPixels(message);
    }

    //Progress handler class
    public interface ProgressHandler {

        void setTotal(int tot);

        void increment(int inc);

        void finished();
    }
}

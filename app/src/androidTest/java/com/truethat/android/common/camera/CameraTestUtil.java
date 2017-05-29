package com.truethat.android.common.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/05/2017 for TrueThat.
 */

public class CameraTestUtil {
    public static final String BITMAP_1x1_PATH = "common/camera/bitmap_1x1.bmp";

    /**
     * Creates a mocked Image from a bitmap underlying byte array.
     *
     * @param bitmapBytes array the is derived from a bitmap image.
     * @param timestamp   of the mocked image.
     * @return mocked Image that is derived from the asset.
     */
    public static Image bitmapBytesToMockedImage(byte[] bitmapBytes,
                                                 long timestamp) throws Exception {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        // Mocking the single image plane.
        Image.Plane mockedPlane = mock(Image.Plane.class);
        when(mockedPlane.getBuffer()).thenReturn(ByteBuffer.wrap(bitmapBytes));
        when(mockedPlane.getPixelStride()).thenReturn(1);
        when(mockedPlane.getRowStride()).thenReturn(bitmap.getWidth());
        // Mocking the image itself
        Image mockedImage = mock(Image.class);
        when(mockedImage.getHeight()).thenReturn(bitmap.getHeight());
        when(mockedImage.getWidth()).thenReturn(bitmap.getWidth());
        when(mockedImage.getFormat()).thenReturn(ImageFormat.FLEX_RGB_888);
        when(mockedImage.getTimestamp()).thenReturn(timestamp);
        when(mockedImage.getCropRect())
                .thenReturn(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
        when(mockedImage.getPlanes()).thenReturn(new Image.Plane[]{mockedPlane});
        return mockedImage;
    }

    static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}

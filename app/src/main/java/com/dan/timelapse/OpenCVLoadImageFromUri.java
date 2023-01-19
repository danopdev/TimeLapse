package com.dan.timelapse;

import static org.opencv.imgcodecs.Imgcodecs.imdecode;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.content.Context;
import android.net.Uri;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.InputStream;

public class OpenCVLoadImageFromUri {
    static Mat load(Context context, Uri uri) {
        try {
            final InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (null == inputStream) return null;
            final byte[] imageRawData = new byte[inputStream.available()];
            inputStream.read(imageRawData);
            inputStream.close();
            Mat image = imdecode(new MatOfByte(imageRawData), Imgcodecs.IMREAD_COLOR | Imgcodecs.IMREAD_ANYDEPTH);
            Mat rgbImage = new Mat();
            cvtColor(image, rgbImage, COLOR_BGR2RGB);
            return rgbImage;
        } catch (Exception e) {
            return null;
        }
    }
}

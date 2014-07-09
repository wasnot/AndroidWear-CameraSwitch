package net.wasnot.wear.cameraswitch;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;

/**
 * Created by aidaakihiro on 2014/07/09.
 */
public class ImageManager {

    private static void saveSdcard(byte[] data) {
        // SDカードにJPEGデータを保存する
        if (data != null) {
            FileOutputStream myFOS = null;
            try {
                myFOS = new FileOutputStream(
                        "/sdcard/camera_test" + System.currentTimeMillis() + ".jpg");
                myFOS.write(data);
                myFOS.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static Bitmap getBitmap(byte[] data) {
        if (data != null) {
            Bitmap bmp = null;
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bmp;
//                        // 読み込む範囲
//                        int previewWidth = camera.getParameters().getPreviewSize().width;
//                        int previewHeight = camera.getParameters().getPreviewSize().height;
//
//                        // プレビューデータから Bitmap を生成
//                        Bitmap bmp = ImageManager.getBitmapImageFromYUV(data, previewWidth,
//                                previewHeight);
        }
        return null;
    }

    public static Uri addImageAsCamera(ContentResolver cr, Bitmap bitmap) {
        long dateTaken = System.currentTimeMillis();
        String name = createName(dateTaken) + ".jpg";
        String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name,
                name);
        return Uri.parse(uriStr);
    }

    private static String createName(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
    }

    public static Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
        return bmp;
    }

    public static final void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10)
                        & 0xff);
            }
        }
    }

}

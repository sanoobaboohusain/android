package led.bluetooth.app.sanoob.com.bluetoothled.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sanoob on 9/5/16.
 */
public class ImageProcessing {

    public String getProminentColor(Bitmap image){

        int color =  theCalculation(image);
        int red=   (color >> 16) & 0xFF;
        int green= (color >> 8) & 0xFF;
        int blue=  (color >> 0) & 0xFF;
        // Toast.makeText(MainActivity.this, "color: "+red+" "+green+" "+blue,Toast.LENGTH_LONG).show();
         String data = red+","+green+","+blue;

        return data;
    }



    protected Integer theCalculation(Bitmap image) {
        Map histogram = new HashMap();

        for(int i = 0; i < image.getWidth(); i++)
            for(int j = 0; j < image.getHeight(); j++) {
                int rgb = image.getPixel(i,j);

                int rgbArray[] = getRGBArrayFromPixel(rgb);

                if(!isGray(rgbArray)) {

                    Integer counter = (Integer) histogram.get(rgb);

                    if (counter == null)
                        counter = 0;
                    counter++;

                    histogram.put(rgb, counter);
                }
            }

        return Color.parseColor("#"+getMostCommonColor(histogram));
    }

    private int[] getRGBArrayFromPixel(int pixel) {

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red,green,blue};

    }

    private boolean isGray(int[] rgbArr) {

        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];

        int tolerance = 100;

        if (rgDiff > tolerance || rgDiff < -tolerance)
            if (rbDiff > tolerance || rbDiff < -tolerance)
                return false;
        return true;
    }

    private String getMostCommonColor(Map histogram) {

        List list = new LinkedList(histogram.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {

                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());

            }

        });

        Map.Entry me = (Map.Entry )list.get(list.size()-1);
        int[] rgb = getRGBArrayFromPixel((Integer) me.getKey());

        return Integer.toHexString(rgb[0])+""+Integer.toHexString(rgb[1])+""+Integer.toHexString(rgb[2]);
    }

}

package led.bluetooth.app.sanoob.com.bluetoothled.bluetooth;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by root on 9/5/16.
 */
public class BluetoothOperation {
    public static OutputStream BTout = null;

    public void writeData(OutputStream out, String data) throws IOException{

        data += "\n";
        out.write(data.getBytes());
    }

    public void setBTOutputStream(OutputStream out){
        BTout = out;
    }

    public OutputStream getBToutputStream(){
        return BTout;
    }
}

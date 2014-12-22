package info.nukoneko.readnfcf;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent =
                PendingIntent.getActivity(
                        this, 0,
                        new Intent(this, this.getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
                );
    }

    public void readNFC(Intent intent) {
        String builder = "";
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NfcF techF = NfcF.get(tag);
        try {
            techF.connect();
            if (techF.isConnected()) {
                ResultPollingObject result = nfcPolling(techF, techF.getSystemCode());
                if(result != null && result.isSuccess()) {
                    ResultReadObject readResult;
                    TextView textView = (TextView)findViewById(R.id.read_result);
                    textView.setText("");

                    builder += "IDm \t- " + byte2StringSpace(result.IDm, " ") + "\n";
                    builder += "PMm \t- " + byte2StringSpace(result.PMm, " ") + "\n";

                    // ------- USER BLOCK --------
                    for(byte i = (byte)0x00; i < (byte)0x0f;i += (byte)0x01) {
                        builder += String.format("%1$02x", i) + " - ";
                        readResult = nfcReadWithoutEncryption(techF, result.IDm, new byte[]{i});
                        if (readResult != null && readResult.isSuccess() && readResult.blockData.size() > 0) {
                            builder += byte2StringSpace(readResult.blockData.get(0), " ") + "\n";
                        }else{
                            builder += "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n";
                        }
                    }

                    for(byte i = (byte)0x80; i < (byte)0x89; i += (byte)0x01) {
                        builder += String.format("%1$02x", i) + " - ";
                        readResult = nfcReadWithoutEncryption(techF, result.IDm, new byte[]{i});
                        if (readResult != null && readResult.isSuccess() && readResult.blockData.size() > 0) {
                            builder += byte2StringSpace(readResult.blockData.get(0), " ") + "\n";
                        }else{
                            builder += "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n";
                        }
                    }

                    System.out.println(builder);
                    textView.setText(builder);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ResultPollingObject {
        private boolean success;
        byte[] IDm = new byte[8];
        byte[] PMm = new byte[8];
        public boolean isSuccess(){
            return this.success;
        }
        public ResultPollingObject(byte[] result) {
            this.success = result[1] == 0x01;
            if (this.success) {
                System.arraycopy(result, 2, this.IDm, 0 , 8);
                System.arraycopy(result, 10, this.PMm, 0 , 8);
            }
        }
    }

    public void writeData_危険(NfcF techF, byte[] IDm){
        nfcWriteWithoutEncryption(techF, IDm, (byte)0x00,
                new byte[]{0x00,0x0A,0x00,0x00,0x00,0x00,(byte)0xFF,(byte)0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x01,
                new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x03,
                new byte[]{0x31,0x32,0x31,0x30,0x30,0x31,0x30,0x30,0x2A,0x43,0x55,0x43,0x00,0x00,0x00,0x00});

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x04,
                new byte[]{0x30,0x32,0x31,0x31,0x34,0x30,0x30,0x39,0x36,0x5F,0x00,0x00,0x00,0x00,0x00,0x00});

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x0e,
                new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF});

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x82,
                new byte[]{
                        IDm[0],IDm[1],IDm[2],IDm[3],
                        IDm[4],IDm[5],IDm[6],IDm[7],
                        0x00, 0x46, 0x00, 0x11, 0x00, 0x00, 0x00, 0x00
                });

        nfcWriteWithoutEncryption(techF, IDm, (byte)0x86,
                new byte[]{0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
    }

    private ResultPollingObject nfcPolling(NfcF nfcF, byte[] SYSTEM_CODE) {
        try {
            return new ResultPollingObject(
                    nfcF.transceive(new byte[]{
                            (byte) 0x06, // data size
                            (byte) 0x00, // command polling -> 00
                            SYSTEM_CODE[0], // system code
                            SYSTEM_CODE[1], // system code
                            (byte) 0x00, // request code
                            (byte) 0x0F // time slot
                    })
            );
        } catch (IOException e) {
            return null;
        }
    }

    private ResultReadObject nfcReadWithoutEncryption(NfcF nfcF, byte[] IDm, byte[] readBlock) {
        try {
            byte[] command = new byte[]{
                    (byte) 0x06, // command polling -> 00
                    IDm[0],
                    IDm[1],
                    IDm[2],
                    IDm[3],
                    IDm[4],
                    IDm[5],
                    IDm[6],
                    IDm[7],
                    (byte) 0x01,
                    (byte) 0x0b,
                    (byte) 0x00,
                    (byte) readBlock.length,};

            int byteSize = command.length + readBlock.length * 2 + 1;
            ByteBuffer byteBuf = ByteBuffer.allocate(byteSize);
            byteBuf.put((byte)(byteSize));
            byteBuf.put(command);
            for ( int i = 0; i < readBlock.length; i++){
                byteBuf.put((byte)0x80);
                byteBuf.put(readBlock[i]);
            }
            return new ResultReadObject(nfcF.transceive(byteBuf.array()));
        } catch (IOException e) {
            return null;
        }
    }
    private ResultReadObject nfcWriteWithoutEncryption(NfcF nfcF, byte[] IDm, byte BLOCK, byte[] writeData) {
        byte[] command = new byte[]{
                (byte) 0x08, // command polling -> 00
                IDm[0],
                IDm[1],
                IDm[2],
                IDm[3],
                IDm[4],
                IDm[5],
                IDm[6],
                IDm[7],
                (byte) 0x01, // service count
                (byte) 0x09, // service code
                (byte) 0x00, // service code
                (byte) 0x01, // WRITE block num
                (byte) 0x80,
                BLOCK};
        int byteSize = command.length + writeData.length + 1;
        ByteBuffer byteBuf = ByteBuffer.allocate(byteSize);
        byteBuf.put((byte)(byteSize));
        byteBuf.put(command);
        byteBuf.put(writeData);

        System.out.println(byte2StringSpace(byteBuf.array(), " "));

        try {
            return new ResultReadObject(
                    nfcF.transceive(byteBuf.array())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class ResultReadObject {
        private boolean success;
        byte[] IDm = new byte[8];
        byte statusFlag1;
        byte statusFlag2;
        int blockNum;
        ArrayList<byte[]> blockData = new ArrayList<>();
        public boolean isSuccess(){
            return this.success;
        }
        public ResultReadObject(byte[] result){
            this.success = result[1] == 0x07;
            if (this.success) {
                System.arraycopy(result, 2, this.IDm, 0, 8);
                this.statusFlag1 = result[10];
                this.statusFlag2 = result[11];
                if(this.statusFlag1 == 0x00){
                    this.blockNum = (int)result[12];
                    for(int i = 0 ; i < this.blockNum; i++){
                        byte[] dat = new byte[16];
                        System.arraycopy(result, 12 * (1 + i) + 1, dat, 0, 16);
                        blockData.add(dat);
                    }
                }
            }
        }
    }

    private void resolveIntent(Intent intent){
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
            readNFC(getIntent());
        }else{
            finish();
        }
    }

    private String byte2String(byte[] bytes) {
        StringBuilder ret = new StringBuilder();
        if ( bytes != null) {
            for (byte aByte : bytes) {
                ret.append(Integer.toHexString(aByte)) ;
            }
        }
        return ret.toString();
    }

    private String byte2StringSpace(byte[] bytes, String separate) {
        StringBuilder ret = new StringBuilder();
        if ( bytes != null) {
            for (byte aByte : bytes) {
                String str = Integer.toHexString(aByte);
                str = (str.length() == 1)? "0" + str:str;
                str = str.replace("ff", "");
                if(str.equals("")) str = "FF";
                ret.append(str.toUpperCase() + separate);
            }
        }
        return ret.toString();
    }

    private String byte2String(byte[] bytes, byte mask) {
        StringBuilder ret = new StringBuilder();
        if ( bytes != null) {
            for (byte aByte : bytes) {
                ret.append(Integer.toHexString(aByte & mask)) ;
            }
        }
        return ret.toString();
    }

    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected  void onPause(){
        super.onPause();
        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

}

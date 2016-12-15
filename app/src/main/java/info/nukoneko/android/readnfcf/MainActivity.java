package info.nukoneko.android.readnfcf;

import android.app.PendingIntent;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

import info.nukoneko.android.readnfcf.databinding.ActivityMainBinding;
import info.nukoneko.android.readnfcf.entity.PollingResultObject;
import info.nukoneko.android.readnfcf.entity.ReadResultObject;


public final class MainActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent =
                PendingIntent.getActivity(
                        this, 0,
                        new Intent(this, this.getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
                );
    }

    private void readNFC(Intent intent) {
        StringBuilder builder = new StringBuilder();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NfcF techF = NfcF.get(tag);
        try {
            techF.connect();
            if (techF.isConnected()) {
                PollingResultObject result = nfcPolling(techF, techF.getSystemCode());
                if(result != null && result.isSuccess()) {
                    ReadResultObject readResult;

                    builder.append("IDm \t- ")
                            .append(byte2StringSpace(result.getIDm(), " "))
                            .append("\n");
                    builder.append("PMm \t- ")
                            .append(byte2StringSpace(result.getPMm(), " "))
                            .append("\n");

                    // ------- USER BLOCK --------
                    for(byte i = (byte)0x00; i < (byte)0x0f;i += (byte)0x01) {
                        builder.append(String.format("%1$02x", i)).append(" - ");
                        readResult = nfcReadWithoutEncryption(techF, result.getIDm(), new byte[]{i});
                        if (readResult != null && readResult.isSuccess() && readResult.getBlockData().size() > 0) {
                            builder.append(byte2StringSpace(readResult.getBlockData().get(0), " ")).append("\n");
                        }else{
                            builder.append("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n");
                        }
                    }

                    for(byte i = (byte)0x80; i < (byte)0x89; i += (byte)0x01) {
                        builder.append(String.format("%1$02x", i)).append(" - ");
                        readResult = nfcReadWithoutEncryption(techF, result.getIDm(), new byte[]{i});
                        if (readResult != null && readResult.isSuccess() && readResult.getBlockData().size() > 0) {
                            builder.append(byte2StringSpace(readResult.getBlockData().get(0), " ")).append("\n");
                        }else{
                            builder.append("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n");
                        }
                    }

                    System.out.println(builder);
                    binding.readResult.setText(builder);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private PollingResultObject nfcPolling(@NonNull NfcF nfcF, byte[] SYSTEM_CODE) {
        try {
            return new PollingResultObject(
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

    @Nullable
    private ReadResultObject nfcReadWithoutEncryption(@NonNull NfcF nfcF, byte[] IDm, byte[] readBlock) {
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
            return new ReadResultObject(nfcF.transceive(byteBuf.array()));
        } catch (IOException e) {
            return null;
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

    @NonNull
    private String byte2StringSpace(byte[] bytes, String separate) {
        StringBuilder ret = new StringBuilder();
        if ( bytes != null) {
            for (byte aByte : bytes) {
                String str = Integer.toHexString(aByte);
                str = (str.length() == 1)? "0" + str:str;
                str = str.replace("ff", "");
                if(str.equals("")) str = "FF";
                ret.append(str.toUpperCase()).append(separate);
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
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected  void onPause(){
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}

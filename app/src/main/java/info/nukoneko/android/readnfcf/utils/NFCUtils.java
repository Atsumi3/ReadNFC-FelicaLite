package info.nukoneko.android.readnfcf.utils;


import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

import info.nukoneko.android.readnfcf.entity.PollingResultObject;
import info.nukoneko.android.readnfcf.entity.ReadResultObject;

public final class NFCUtils {
    private NFCUtils(){}

    @NonNull public static String readNFC(Intent intent) {
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
                    return builder.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Nullable
    private static PollingResultObject nfcPolling(@NonNull NfcF nfcF, byte[] SYSTEM_CODE) {
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
    private static ReadResultObject nfcReadWithoutEncryption(@NonNull NfcF nfcF, byte[] IDm, byte[] readBlock) {
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

    @NonNull
    private static String byte2StringSpace(byte[] bytes, String separate) {
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
}

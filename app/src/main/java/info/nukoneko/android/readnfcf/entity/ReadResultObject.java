package info.nukoneko.android.readnfcf.entity;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public final class ReadResultObject {
    private final boolean success;
    private final byte[] IDm = new byte[8];
    private final byte statusFlag1;
    private final byte statusFlag2;
    private final int blockNum;
    @NonNull private final ArrayList<byte[]> blockData;

    public ReadResultObject(byte[] result){
        this.success = result[1] == 0x07;
        ArrayList<byte[]> blocks = new ArrayList<>();
        if (this.success) {
            System.arraycopy(result, 2, this.IDm, 0, 8);
            this.statusFlag1 = result[10];
            this.statusFlag2 = result[11];
            if(this.statusFlag1 == 0x00){
                this.blockNum = (int)result[12];
                for(int i = 0 ; i < this.blockNum; i++){
                    byte[] dat = new byte[16];
                    System.arraycopy(result, 12 * (1 + i) + 1, dat, 0, 16);
                    blocks.add(dat);
                }
            } else {
                this.blockNum = 0;
            }
        } else {
            this.statusFlag1 = 0x00;
            this.statusFlag2 = 0x00;
            this.blockNum = 0;
        }
        this.blockData = blocks;
    }

    public boolean isSuccess(){
        return this.success;
    }

    public byte[] getIDm() {
        return IDm;
    }

    public byte getStatusFlag1() {
        return statusFlag1;
    }

    public byte getStatusFlag2() {
        return statusFlag2;
    }

    public int getBlockNum() {
        return blockNum;
    }

    @NonNull
    public ArrayList<byte[]> getBlockData() {
        return blockData;
    }
}

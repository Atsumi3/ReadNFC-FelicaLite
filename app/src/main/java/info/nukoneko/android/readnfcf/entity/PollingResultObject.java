package info.nukoneko.android.readnfcf.entity;

public final class PollingResultObject {
    private boolean success;
    private final byte[] IDm = new byte[8];
    private final byte[] PMm = new byte[8];

    public PollingResultObject(byte[] result) {
        this.success = result[1] == 0x01;
        if (this.success) {
            System.arraycopy(result, 2, this.IDm, 0 , 8);
            System.arraycopy(result, 10, this.PMm, 0 , 8);
        }
    }

    public byte[] getIDm() {
        return IDm;
    }

    public byte[] getPMm() {
        return PMm;
    }

    public boolean isSuccess(){
        return this.success;
    }
}

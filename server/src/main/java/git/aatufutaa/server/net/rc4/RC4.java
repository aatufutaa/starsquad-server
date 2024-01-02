package git.aatufutaa.server.net.rc4;

public class RC4 {

    private static final int SBOX_LENGTH = 256;

    private final byte[] key;
    private final int[] sbox;

    public RC4(byte[] key) {
        this.key = key;
        this.sbox = new int[SBOX_LENGTH];
        this.init();
    }

    private void init() {
        int j = 0;

        for (int i = 0; i < SBOX_LENGTH; i++) {
            this.sbox[i] = i;
        }

        for (int i = 0; i < SBOX_LENGTH; i++) {
            j = (j + this.sbox[i] + (this.key[i % this.key.length]) & 0xFF) % SBOX_LENGTH;
            this.swap(i, j, this.sbox);
        }
    }

    public void crypt(byte[] inBuf, int inOffset, byte[] outBuf, int outOffset, int length) {
        int i = 0;
        int j = 0;
        for (int n = 0; n < length; n++) {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + this.sbox[i]) % SBOX_LENGTH;
            this.swap(i, j, this.sbox);
            int rand = this.sbox[(this.sbox[i] + this.sbox[j]) % SBOX_LENGTH];
            outBuf[outOffset + n] = (byte) (rand ^ inBuf[inOffset + n]);
        }
    }

    private void swap(int i, int j, int[] sbox) {
        int temp = sbox[i];
        sbox[i] = sbox[j];
        sbox[j] = temp;
    }
}

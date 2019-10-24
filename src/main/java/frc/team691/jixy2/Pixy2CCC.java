package frc.team691.jixy2;

import java.nio.ByteBuffer;

public class Pixy2CCC {
    public static final int CCC_RESPONSE_BLOCKS          = 0x21;
    public static final int CCC_REQUEST_BLOCKS           = 0x20;

    public static final int CCC_MAX_SIGNATURE            = 7;
    public static final int CCC_SIG1                     = 1;
    public static final int CCC_SIG2                     = 2;
    public static final int CCC_SIG3                     = 4;
    public static final int CCC_SIG4                     = 8;
    public static final int CCC_SIG5                     = 16;
    public static final int CCC_SIG6                     = 32;
    public static final int CCC_SIG7                     = 64;
    public static final int CCC_COLOR_CODES              = 128;
    public static final int CCC_SIG_ALL                  = 0xff;

    public static final int CCC_BLOCK_SIZE               = 14;
    public static final int CCC_DEFAULT_MAX_BLOCKS       = 255;

    private Pixy2 pixy;

    public int numBlocks;
    public PBlock[] blocks;

    public Pixy2CCC(Pixy2 pixy) {
        this.pixy = pixy;
    }

    public int getBlocks() {
        return getBlocks(CCC_SIG_ALL, CCC_DEFAULT_MAX_BLOCKS);
    }

    public int getBlocks(int sigmap, int maxBlocks) {
        Pixy2Transaction txn = new Pixy2Transaction(CCC_REQUEST_BLOCKS, 2, maxBlocks * CCC_BLOCK_SIZE);
        txn.sendPacket.addByte(sigmap).addByte(maxBlocks);
        if (!pixy.executeTransaction(txn) || txn.recvPacket.type != CCC_RESPONSE_BLOCKS) {
            return Pixy2.PIXY_RESULT_ERROR;
        }
        blocks = null;
        numBlocks = txn.recvPacket.payloadSize / CCC_BLOCK_SIZE;
        if (numBlocks > 0) {
            blocks = new PBlock[numBlocks];
            for (int i = 0; i < numBlocks; i++) {
                blocks[i] = new PBlock(txn.recvPacket.payload);
            }
        }
        return numBlocks;
    }

    public static class PBlock {
        public int signature;
        public int x;
        public int y;
        public int width;
        public int height;
        public int angle;
        public int index;
        public int age;

        public PBlock() {
        }

        public PBlock(ByteBuffer payload) {
            update(payload);
        }

        public void update(ByteBuffer payload) {
            signature = payload.getShort();
            x = payload.getShort();
            y = payload.getShort();
            width = payload.getShort();
            height = payload.getShort();
            angle = payload.getShort();
            index = payload.get();
            age = payload.get();
        }

        public String toString() {
            if (signature > CCC_MAX_SIGNATURE) { // color code! (CC)
                // convert signature number to an octal string
                StringBuilder sig = new StringBuilder(6);
                boolean flag = false;
                char d;
                for (int i = 12; i >= 0; i -= 3) {
                    d = (char) ((signature >> i) & 0x07);
                    if (d > 0 && !flag) {
                        flag = true;
                    }
                    if (flag) {
                        sig.append(d + '0');
                    }
                }
                return String.format(
                    "CC block sig: %s (%d decimal) x: %d y: %d width: %d height: %d angle: %d index: %d age: %d",
                    sig.toString(), signature, x, y, width, height, angle, index, age);
            }
            // regular block.  Note, angle is always zero, so no need to print
            return String.format("sig: %d x: %d y: %d width: %d height: %d index: %d age: %d",
                signature, x, y, width, height, index, age);
        }
    }
}

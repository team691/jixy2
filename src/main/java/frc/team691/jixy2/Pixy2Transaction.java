package frc.team691.jixy2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Pixy2Transaction {
    public static final int PIXY_NO_CHECKSUM_SYNC               = 0xc1ae;
    public static final int PIXY_CHECKSUM_SYNC                  = 0xc1af;
    public static final int PIXY_SEND_HEADER_SIZE               = 4;
    public static final int PIXY_RECV_HEADER_SIZE               = 6;

    public Pbuffer sendPacket;
    public Pbuffer recvPacket;

    public Pixy2Transaction(int type, int reqPayloadSize, int resPayloadSize) {
        sendPacket = new Pbuffer(type, reqPayloadSize);
        recvPacket = new Pbuffer(resPayloadSize + PIXY_RECV_HEADER_SIZE);
    }

    public static class Pbuffer {
        private ByteBuffer buf;

        int type;
        int payloadSize;
        int headerSize;
        boolean isRequest;
        ByteBuffer payload;

        public Pbuffer(int capacity) {
            buf = ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
        }

        // SendPacket
        public Pbuffer(int type, int payloadSize) {
            this(payloadSize + PIXY_SEND_HEADER_SIZE);
            addShort(PIXY_NO_CHECKSUM_SYNC).addByte(type).addByte(payloadSize);
            this.payload = buf.slice().order(ByteOrder.LITTLE_ENDIAN);
            this.type = type;
            this.payloadSize = payloadSize;
            this.isRequest = true;
        }

        // RecvPacket
        public Pbuffer(int type, int payloadSize, int checksum) {
            this(payloadSize + PIXY_RECV_HEADER_SIZE);
            addShort(PIXY_CHECKSUM_SYNC).addByte(type).addByte(payloadSize).addShort(checksum);
            this.payload = buf.slice().order(ByteOrder.LITTLE_ENDIAN);
            this.type = type;
            this.payloadSize = payloadSize;
            this.isRequest = false;
        }

        public ByteBuffer getByteBuffer() {
            return buf;
        }

        public Pbuffer updateFromBuffer() {
            isRequest = buf.getShort(0) == (short) PIXY_NO_CHECKSUM_SYNC;
            type = Byte.toUnsignedInt(buf.get(2));
            payloadSize = Byte.toUnsignedInt(buf.get(3));
            payload = buf.position(isRequest ? PIXY_SEND_HEADER_SIZE : PIXY_RECV_HEADER_SIZE).slice()
                .order(ByteOrder.LITTLE_ENDIAN);
            return this;
        }

        public Pbuffer addByte(int b) {
            buf.put((byte) b);
            return this;
        }
    
        public Pbuffer addShort(int s) {
            buf.putShort((short) s);
            return this;
        }
    }
}

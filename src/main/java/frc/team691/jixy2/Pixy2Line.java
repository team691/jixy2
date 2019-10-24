package frc.team691.jixy2;

import java.nio.ByteBuffer;

public class Pixy2Line {
    public static final int LINE_REQUEST_GET_FEATURES               = 0x30;
    public static final int LINE_RESPONSE_GET_FEATURES              = 0x31;
    public static final int LINE_REQUEST_SET_MODE                   = 0x36;
    public static final int LINE_REQUEST_SET_VECTOR                 = 0x38;
    public static final int LINE_REQUEST_SET_NEXT_TURN_ANGLE        = 0x3a;
    public static final int LINE_REQUEST_SET_DEFAULT_TURN_ANGLE     = 0x3c;
    public static final int LINE_REQUEST_REVERSE_VECTOR             = 0x3e;

    public static final int LINE_GET_MAIN_FEATURES                  = 0x00;
    public static final int LINE_GET_ALL_FEATURES                   = 0x01;

    public static final int LINE_MODE_TURN_DELAYED                  = 0x01;
    public static final int LINE_MODE_MANUAL_SELECT_VECTOR          = 0x02;
    public static final int LINE_MODE_WHITE_LINE                    = 0x80;

    // Features
    public static final int LINE_VECTOR                             = 0x01;
    public static final int LINE_INTERSECTION                       = 0x02;
    public static final int LINE_BARCODE                            = 0x04;
    public static final int LINE_ALL_FEATURES = (LINE_VECTOR | LINE_INTERSECTION |LINE_BARCODE);

    public static final int LINE_VECTOR_SIZE                        = 6;
    public static final int LINE_INTERSECTION_SIZE                  = 4;
    public static final int LINE_BARCODE_SIZE                       = 4;

    public static final int LINE_FLAG_INVALID                       = 0x02;
    public static final int LINE_FLAG_INTERSECTION_PRESENT          = 0x04;

    public static final int LINE_MAX_INTERSECTION_LINES             = 6;

    private Pixy2 pixy;

    public int numVectors;
    public PVector[] vectors;
    //public int numIntersections;
    //public Pintersection[] intersections;
    //public Pbarcode[] barcodes;
    //public int numBarcodes;

    public Pixy2Line(Pixy2 pixy) {
        this.pixy = pixy;
    }

    public int getMainFeatures() {
        return getMainFeatures(LINE_ALL_FEATURES);
    }

    public int getMainFeatures(int features) {
        return getFeatures(LINE_GET_MAIN_FEATURES, features);
    }

    public int getAllFeatures() {
        return getAllFeatures(LINE_ALL_FEATURES);
    }

    public int getAllFeatures(int features) {
        return getFeatures(LINE_GET_ALL_FEATURES, features);
    }

    public int getFeatures(int type, int features) {
        Pixy2Transaction txn = new Pixy2Transaction(LINE_REQUEST_GET_FEATURES, 2, 100);
        txn.sendPacket.addByte(type).addByte(features);
        if (!pixy.executeTransaction(txn) || txn.recvPacket.type != LINE_RESPONSE_GET_FEATURES) {
            return Pixy2.PIXY_RESULT_ERROR;
        }
        ByteBuffer payload = txn.recvPacket.payload;
        int i, ft, len, res = 0;
        numVectors = /*numIntersections = numBarcodes =*/ 0;
        vectors = /*intersections = barcodes =*/ null;
        while ((ft = payload.get()) > 0) {
            len = payload.get();
            //System.out.format("%d %d\n", ft, len);
            switch (ft) {
                case LINE_VECTOR :
                    len /= LINE_VECTOR_SIZE;
                    numVectors = len;
                    vectors = new PVector[len];
                    for (i = 0; i < len; i++) {
                        vectors[i] = new PVector(payload);
                    }
                    res |= LINE_VECTOR;
                    break;
                case LINE_INTERSECTION :
                    len /= LINE_INTERSECTION_SIZE;
                    //numIntersections = len;
                    //intersections = new Pintersection[len];
                    for (i = 0; i < len; i++) {
                        //intersections[i] = new Pintersection(payload);
                        payload.getInt();
                    }
                    res |= LINE_INTERSECTION;
                    break;
                case LINE_BARCODE :
                    len /= LINE_BARCODE_SIZE;
                    //numBarcodes = len;
                    //barcodes = new Pbarcode[len];
                    for (i = 0; i < len; i++) {
                        //barcodes[i] = new Pbarcode(payload);
                        payload.getInt();
                    }
                    res |= LINE_BARCODE;
                    break;
            }
        }
        return res;
    }

    public int setMode(int mode) {
        Pixy2Transaction txn = new Pixy2Transaction(LINE_REQUEST_SET_MODE, 1, 4);
        txn.sendPacket.addByte(mode);
        if (!pixy.executeTransaction(txn) || txn.recvPacket.type != Pixy2.PIXY_TYPE_RESPONSE_RESULT) {
            return Pixy2.PIXY_RESULT_ERROR;
        }
        return txn.recvPacket.payload.getInt();
    }

    public static class PVector {
        public int x0;
        public int y0;
        public int x1;
        public int y1;
        public int index;
        public int flags;

        public PVector() {
        }
        
        public PVector(ByteBuffer payload) {
            update(payload);
        }

        public void update(ByteBuffer payload) {
            x0 = payload.get();
            y0 = payload.get();
            x1 = payload.get();
            y1 = payload.get();
            index = payload.get();
            flags = payload.get();
        }

        public String toString() {
            return String.format("vector: (%d %d) (%d %d) index: %d flags %d", x0, y0,
                x1, y1, index, flags);
        }
    }
}

package frc.team691.jixy2;

public class Pixy2 {
    public static final int PIXY_DEFAULT_ARGVAL                 = 0x80000000;
    public static final int PIXY_BUFFERSIZE                     = 0x104;
    public static final int PIXY_MAX_PROGNAME                   = 33;

    public static final int PIXY_TYPE_REQUEST_CHANGE_PROG       = 0x02;
    public static final int PIXY_TYPE_REQUEST_RESOLUTION        = 0x0c;
    public static final int PIXY_TYPE_RESPONSE_RESOLUTION       = 0x0d;
    public static final int PIXY_TYPE_REQUEST_VERSION           = 0x0e;
    public static final int PIXY_TYPE_RESPONSE_VERSION          = 0x0f;
    public static final int PIXY_TYPE_RESPONSE_RESULT           = 0x01;
    public static final int PIXY_TYPE_RESPONSE_ERROR            = 0x03;
    public static final int PIXY_TYPE_REQUEST_BRIGHTNESS        = 0x10;
    public static final int PIXY_TYPE_REQUEST_SERVO             = 0x12;
    public static final int PIXY_TYPE_REQUEST_LED               = 0x14;
    public static final int PIXY_TYPE_REQUEST_LAMP              = 0x16;
    public static final int PIXY_TYPE_REQUEST_FPS               = 0x18;

    public static final int PIXY_RESULT_OK                      = 0;
    public static final int PIXY_RESULT_ERROR                   = -1;
    public static final int PIXY_RESULT_BUSY                    = -2;
    public static final int PIXY_RESULT_CHECKSUM_ERROR          = -3;
    public static final int PIXY_RESULT_TIMEOUT                 = -4;
    public static final int PIXY_RESULT_BUTTON_OVERRIDE         = -5;
    public static final int PIXY_RESULT_PROG_CHANGING           = -6;

    private DeviceLink deviceLink;

    public int frameWidth = 0;
    public int frameHeight = 0;
    public Pversion version;

    public Pixy2Line line;

    public Pixy2(DeviceLink devLink) {
        deviceLink = devLink;
        line = new Pixy2Line(this);
    }

    public int changeProg(String prog) {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_CHANGE_PROG, PIXY_MAX_PROGNAME, 4);
        if (prog.length() > PIXY_MAX_PROGNAME) {
            prog = prog.substring(0, PIXY_MAX_PROGNAME);
        }
        txn.sendPacket.payload.put(prog.getBytes());
        if (!executeTransaction(txn)) {
            return PIXY_RESULT_ERROR;
        }
        getResolution();
        return txn.recvPacket.payload.getInt();
    }

    public int setLED(int r, int g, int b) {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_LED, 3, 4);
        txn.sendPacket.addByte(r).addByte(g).addByte(b);
        if (!executeTransaction(txn) || txn.recvPacket.type != PIXY_TYPE_RESPONSE_RESULT) {
            return PIXY_RESULT_ERROR;
        }
        return txn.recvPacket.payload.getInt();
    }

    public int setLamp(int upper, int lower) {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_LAMP, 2, 4);
        txn.sendPacket.addByte(upper).addByte(lower);
        if (!executeTransaction(txn) || txn.recvPacket.type != PIXY_TYPE_RESPONSE_RESULT) {
            return PIXY_RESULT_ERROR;
        }
        return txn.recvPacket.payload.getInt();
    }

    public int setCameraBrightness(int brightness) {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_BRIGHTNESS, 1, 4);
        txn.sendPacket.addByte(brightness);
        if (!executeTransaction(txn) || txn.recvPacket.type != PIXY_TYPE_RESPONSE_RESULT) {
            return PIXY_RESULT_ERROR;
        }
        return txn.recvPacket.payload.getInt();
    }

    public int getVersion() {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_VERSION, 0, 50);
        if (!executeTransaction(txn) || txn.recvPacket.type != PIXY_TYPE_RESPONSE_VERSION) {
            return PIXY_RESULT_ERROR;
        }
        version = new Pversion(txn.recvPacket.payload);
        return PIXY_RESULT_OK;
    }

    public int getResolution() {
        Pixy2Transaction txn = new Pixy2Transaction(PIXY_TYPE_REQUEST_RESOLUTION, 1, 4);
        if (!executeTransaction(txn) || txn.recvPacket.type != PIXY_TYPE_RESPONSE_RESOLUTION) {
            return PIXY_RESULT_ERROR;
        }
        frameWidth = txn.recvPacket.payload.getShort();
        frameHeight = txn.recvPacket.payload.getShort();
        return PIXY_RESULT_OK;
    }

    boolean executeTransaction(Pixy2Transaction txn) {
        return deviceLink.transact(txn);
    }

    public static class Pversion {
        int hardware;
        int firmwareMajor;
        int firmwareMinor;
        int  firmwareBuild;
        String firmwareType;

        public Pversion(java.nio.ByteBuffer payload) {
            char c;
            StringBuilder sb = new StringBuilder();
            hardware = payload.getShort();
            firmwareMajor = payload.get();
            firmwareMinor = payload.get();
            firmwareBuild = payload.getShort();
            while ((c = payload.getChar()) != 0) {
                sb.append(c);
            }
            firmwareType = sb.toString();
        }

        public String toString() {
            return String.format("hardware ver: 0x%x firmware ver: %d.%d.%d %s",
                hardware, firmwareMajor, firmwareMinor, firmwareBuild, firmwareType);
        }
    }
}

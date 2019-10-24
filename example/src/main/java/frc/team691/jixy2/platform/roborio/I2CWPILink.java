package frc.team691.jixy2.platform.roborio;

import java.nio.ByteBuffer;
import edu.wpi.first.wpilibj.I2C;
import frc.team691.pixy2.*;

public class I2CWPILink implements DeviceLink {
    private static final I2C.Port DEFAULT_PORT  = I2C.Port.kOnboard;
    private static final int DEFAULT_ADDRESS    = 0x54;

    private I2C.Port port;
    private int address;
    private I2C i2cLink;

    public I2CWPILink() {
        this(DEFAULT_ADDRESS);
    }

    public I2CWPILink(int address) {
        this(DEFAULT_PORT, address);
    }

    public I2CWPILink(I2C.Port port, int address) {
        this.port = port;
        this.address = address;
        this.i2cLink = new I2C(port, address);
    }

    public boolean isConnected() {
        return !i2cLink.addressOnly();
    }

    @Override
    public boolean transact(Pixy2Transaction txn) {
        while (!isConnected()) {
            i2cLink.close();
            i2cLink = new I2C(port, address);
        }
        ByteBuffer request = txn.sendPacket.getByteBuffer();
        ByteBuffer response = txn.recvPacket.getByteBuffer();
        boolean res = i2cLink.transaction(request, request.capacity(), response, response.capacity());
        txn.recvPacket.updateFromBuffer();
        return !res;
    }
}

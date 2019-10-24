package frc.team691.pixy2bot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team691.jixy2.Pixy2;
import frc.team691.jixy2.Pixy2Line;
import frc.team691.jixy2.Pixy2Line.PVector;
import frc.team691.jixy2.platform.roborio.I2CWPILink;

public class Robot extends TimedRobot {
    Pixy2 pixy;

    @Override
    public void robotInit() {
        pixy = new Pixy2(new I2CWPILink());
        pixy.getVersion();
        SmartDashboard.putString("Pixy version", (pixy.version == null ? "???" : pixy.version.toString()));
        pixy.getResolution();
        SmartDashboard.putString("Pixy res", String.format("%dx%d", pixy.frameWidth, pixy.frameHeight));
        pixy.setLED(255, 0, 0);
        pixy.line.setMode(Pixy2Line.LINE_MODE_WHITE_LINE);
    }

    @Override
    public void robotPeriodic() {
        pixy.line.getMainFeatures();
        
        if (pixy.line.vectors != null) {
            PVector vector = pixy.line.vectors[0];
            SmartDashboard.putString("Vector", vector.toString());
        }
    }
}

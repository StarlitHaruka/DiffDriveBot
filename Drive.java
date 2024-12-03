//./gradlew sA

package robot.drive;

import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.List;
import java.util.function.DoubleSupplier;
import robot.Ports;

public class Drive extends SubsystemBase {

  private final DifferentialDriveOdometry odometry;
  private final CANSparkMax rightLeader =
      new CANSparkMax(Ports.Drive.RIGHT_LEADER, MotorType.kBrushless);
  private final CANSparkMax leftLeader =
      new CANSparkMax(Ports.Drive.LEFT_LEADER, MotorType.kBrushless);
  private final CANSparkMax rightFollower =
      new CANSparkMax(Ports.Drive.RIGHT_FOLLOWER, MotorType.kBrushless);
  private final CANSparkMax leftFollower =
      new CANSparkMax(Ports.Drive.LEFT_FOLLOWER, MotorType.kBrushless);
  private final RelativeEncoder leftEncoder = leftLeader.getEncoder();
  private final RelativeEncoder rightEncoder = rightLeader.getEncoder();
  private final AnalogGyro gyro = new AnalogGyro(Ports.Drive.GYRO_CHANNEL);

  private final DifferentialDrivetrainSim driveSim;





  //initalization
  public Drive() {

    
    driveSim =
    new DifferentialDrivetrainSim(
        DCMotor.getMiniCIM(2),
        DriveConstants.GEARING,
        DriveConstants.MOI,
        DriveConstants.DRIVE_MASS,
        DriveConstants.WHEEL_RADIUS,
        DriveConstants.TRACK_WIDTH,
        DriveConstants.STD_DEVS);  
    
    for (CANSparkMax spark : List.of(leftLeader, leftFollower, rightLeader, rightFollower)) {
      spark.restoreFactoryDefaults();
      spark.setIdleMode(IdleMode.kBrake);
    }

    odometry = new DifferentialDriveOdometry(
    new Rotation2d(), 
    0, 
    0, 
    new Pose2d());

    gyro.reset();

    leftEncoder.setPositionConversionFactor(DriveConstants.POSITION_FACTOR);
    rightEncoder.setPositionConversionFactor(DriveConstants.POSITION_FACTOR);

    leftEncoder.setVelocityConversionFactor(DriveConstants.VELOCITY_FACTOR);
    rightEncoder.setVelocityConversionFactor(DriveConstants.VELOCITY_FACTOR);

    leftEncoder.setPosition(0);
    rightEncoder.setPosition(0);
  
    rightFollower.follow(rightLeader);
    leftFollower.follow(leftLeader);

    leftLeader.setInverted(true);

    }

  private void drive(double leftSpeed, double rightSpeed) {
    leftLeader.set(leftSpeed);
    rightLeader.set(rightSpeed);
  }

  public Command drive(DoubleSupplier vLeft, DoubleSupplier vRight) {
    return run(() -> drive(vLeft.getAsDouble(), vRight.getAsDouble()));
  }

  public void updateOdometry(Rotation2d rotation) {
    odometry.update(rotation, leftEncoder.getPosition(), rightEncoder.getPosition());
  }

  @Override 
  public void periodic() {
    updateOdometry(gyro.getRotation2d());
  }

  public Pose2d pose() {
    return odometry.getPoseMeters();
  }

 
  
}
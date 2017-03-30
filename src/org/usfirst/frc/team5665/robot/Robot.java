
package org.usfirst.frc.team5665.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.vision.VisionRunner;
import edu.wpi.first.wpilibj.vision.VisionThread;

import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team5665.robot.commands.AutoLeftForward;
import org.usfirst.frc.team5665.robot.commands.AutoLeftGear;
import org.usfirst.frc.team5665.robot.commands.AutoMiddleGear;
import org.usfirst.frc.team5665.robot.commands.AutoRightForward;
import org.usfirst.frc.team5665.robot.commands.AutoRightGear;
import org.usfirst.frc.team5665.robot.subsystems.Climber;
import org.usfirst.frc.team5665.robot.subsystems.Drive;
import org.usfirst.frc.team5665.robot.subsystems.FuelCollector;
import org.usfirst.frc.team5665.robot.subsystems.GearHolder;
import org.usfirst.frc.team5665.robot.subsystems.Ramp;
import org.usfirst.frc.team5665.robot.RobotMap;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	public static Drive drive;
	public static Climber climber;
	public static FuelCollector fuelCollector;
	public static GearHolder gearHolder;
	public static Ramp ramp;
	public static OI oi;
	
	public static boolean calibrateEnabled;
	
	private static final int IMG_WIDTH = 320;
	private static final int IMG_HEIGHT = 240;
	
	private VisionThread visionThread;
	private double centerX = 0.0;
	private RobotDrive drive;
	
	public final Object imgLock = new Object();
	
	Command autonomousCommand;
	SendableChooser<Command> chooser = new SendableChooser<>();

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		RobotMap.init();
		drive = new Drive();
		climber = new Climber();
		fuelCollector = new FuelCollector();
		gearHolder = new GearHolder();
		ramp = new Ramp();
		oi = new OI();
		
		calibrateEnabled = false;
		
		chooser.addDefault("Middle", new AutoMiddleGear());
		chooser.addObject("Left Charge", new AutoLeftForward());
		chooser.addObject("Right Charge", new AutoRightForward());
		chooser.addObject("Left Gear", new AutoLeftGear());
		chooser.addObject("Right Gear", new AutoRightGear());
		//Debug commands
		SmartDashboard.putData("Auto Mode", chooser);
		
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		camera.setResolution(IMG_WIDTH, IMG_HEIGHT);
    
    		visionThread = new VisionThread(camera, new Pipeline(), pipeline -> {
        		if (!(pipeline.filterContoursOutput().size() < 2)) {
            			Rect r1 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				Rect r2 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
            			synchronized (imgLock) {
                		centerX1 = r1.x + (r1.width / 2);
				centerX2 = r2.x + (r2.width / 2);
            			}
        		}
    		});
    		visionThread.start();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
	 */
	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		autonomousCommand = chooser.getSelected();

		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		// schedule the autonomous command (example)
		if (autonomousCommand != null)
			autonomousCommand.start();
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();
	}

	@Override
	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null)
			autonomousCommand.cancel();
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
}

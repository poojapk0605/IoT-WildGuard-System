package programmingtheiot.gda.system;
import java.io.File;
import programmingtheiot.common.ConfigConst;

// The `SystemDiskUtilTask` class calculates the disk utilization of the system by measuring the total and available disk space on the root directory. It extends `BaseSystemUtilTask` to provide disk-related telemetry data for monitoring system performance.


public class SystemDiskUtilTask extends BaseSystemUtilTask
{
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemDiskUtilTask()
	{
		super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	}
	
	// public methods
	
	@Override
	public float getTelemetryValue()
	{
		// Get the root directory to monitor disk utilization
		File root = new File("/"); // You can specify a different drive/path if needed
		
		// Get total and free space in bytes
		long totalSpace = root.getTotalSpace();
		long freeSpace = root.getUsableSpace();
		
		// Calculate disk utilization (used space / total space)
		float diskUtil = 0.0f;
		if (totalSpace > 0) {
			diskUtil = (float) (totalSpace - freeSpace) / totalSpace;
		}
		
		return diskUtil;
	}
}
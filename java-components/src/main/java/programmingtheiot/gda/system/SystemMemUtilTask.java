/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;


import java.util.logging.Logger;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import programmingtheiot.common.ConfigConst;

/**
 * The `SystemMemUtilTask` class retrieves and calculates the current heap memory utilization of the system, returning it as a percentage. It extends `BaseSystemUtilTask` to provide memory-related telemetry data for system performance monitoring.
 * 
 */
public class SystemMemUtilTask extends BaseSystemUtilTask
{
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemMemUtilTask()
	{
		super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	}
	
	
	// public methods
	
	@Override
	public float getTelemetryValue()
	{
		MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		double memUtil = ((double) memUsage.getUsed() / (double) memUsage.getMax()) * 100.0d;
		return (float)memUtil;
	}
	
}

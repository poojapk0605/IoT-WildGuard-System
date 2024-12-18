/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import programmingtheiot.common.ConfigConst;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Logger;



/**
 * The `SystemCpuUtilTask` class measures the current CPU utilization of the system by retrieving the system load average through the `OperatingSystemMXBean`. It extends `BaseSystemUtilTask` to provide CPU-related telemetry data for system performance monitoring.
 * 
 */
public class SystemCpuUtilTask extends BaseSystemUtilTask
{
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemCpuUtilTask()
	{
		super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	}
	
	
	// public methods
	
	@Override
	public float getTelemetryValue()
	{     
		// ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
	    double cpuUtil = mxBean.getSystemLoadAverage();
	   
	    return (float) cpuUtil;
		
	//    return 0.0f;
	
	}
	
}

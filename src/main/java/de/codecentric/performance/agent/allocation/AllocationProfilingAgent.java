package de.codecentric.performance.agent.allocation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import de.codecentric.performance.agent.allocation.mbean.Agent;

/**
 * Class registered as premain hook, will add a ClassFileTransformer and register an MBean for controlling the agent.
 */
public class AllocationProfilingAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    String prefix = agentArgs;
    if (prefix == null || prefix.length() == 0) {
      AgentLogger.log("Agent failed to start: Please provide a package prefix to filter.");
      return;
    }
    // accepts both . and / notation, but will convert dots to slashes
    prefix = prefix.replace(".", "/");
    if (!prefix.contains("/")) {
      AgentLogger.log("Agent failed to start: Please provide at least one package level prefix to filter.");
      return;
    }
    registerMBean();
    inst.addTransformer(new AllocationTrackerClassFileTransformer(prefix));
  }

  /*
   * Starts a new thread which will try to connect to the Platform Mbean Server.
   */
  private static void registerMBean() {
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          // retry up to a maximum of 10 minutes
          int retryLimit = 60;
          MBeanServer mbs = null;
          while (mbs == null) {
            if (retryLimit-- == 0) {
              AgentLogger.log("Could not register Agent MBean in 10 minutes.");
              return;
            }
            TimeUnit.SECONDS.sleep(10);
            mbs = ManagementFactory.getPlatformMBeanServer();
          }
          mbs.registerMBean(new Agent(), new ObjectName("de.codecentric:type=Agent"));
          AgentLogger.log("Registered Agent MBean.");
        } catch (Exception e) {
          AgentLogger.log("Could not register Agent MBean. Exception:");
          StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          AgentLogger.log(sw.toString());
        }
      }
    };
    thread.start();
  }
}

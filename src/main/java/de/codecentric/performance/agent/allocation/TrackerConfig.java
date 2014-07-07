package de.codecentric.performance.agent.allocation;

/**
 * Configuration class for the Tracker being used.
 */
public class TrackerConfig {

  /* Package prefix of the agent. To be filtered to avoid potential recursions or other issues. */
  public static final String AGENT_PACKAGE_PREFIX = "de/codecentric/performance/agent/";

  /* Tracker class needs to be slash separated package name. */
  static final String TRACKER_CLASS = "de/codecentric/performance/agent/allocation/Tracker";

  /* Static method to be invoked for each construction. */
  public static final String TRACKER_CALLBACK = "constructed";

  /* Signature of the callback method. Should accept a single String. */
  public static final String TRACKER_CALLBACK_SIGNATURE = "(Ljava/lang/String;)V";

  /*
   * Default number of top classes returned in buildTopList when the argument is <= 0.
   * 
   * Convenience only, no performance impact.
   */
  static final int DEFAULT_AMOUNT = 100;

  /*
   * Default size is insufficient in almost all real world scenarios. Any number is a pure guess. 1000 is a good
   * starting point.
   * 
   * Impacts memory usage.
   */
  static final int MAP_SIZE = 1000;

  /*
   * Default load factor of 0.75 should work fine.
   * 
   * Impacts memory usage. Low values impact cpu usage.
   */
  static final float LOAD_FACTOR = 0.75f;

  /*
   * Default concurrency level of 16 threads is probably sufficient in most real world deployments. Note that the
   * setting is for updating threads only, thus is concerned only when a tracked class is instantiated the first time.
   * 
   * Impacts memory and cpu usage.
   */
  static final int CONCURRENCY_LEVEL = 16;

}

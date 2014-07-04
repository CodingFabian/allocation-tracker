package de.codecentric.performance.agent.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main class, which is notified by BCI inserted code when an object is constructed. This class keeps a
 * ConcurrentHashMap with class names as keys. This is "leaking" the class name by design, so that the class name string
 * is kept even when the class has been unloaded. For each class name the ConcurrentHashMap will store an
 * AtmomicLongInstance.
 */
public class Tracker {

  /*
   * Default number of top classes returned in buildTopList when the argument is <= 0.
   * 
   * Convenience only, no performance impact.
   */
  private static final int DEFAULT_AMOUNT = 100;

  /*
   * Default size is insufficient in almost all real world scenarios. Any number is a pure guess. 1000 is a good
   * starting point.
   * 
   * Impacts memory usage.
   */
  private static final int MAP_SIZE = 1000;

  /*
   * Default load factor of 0.75 should work fine.
   * 
   * Impacts memory usage. Low values impact cpu usage.
   */
  private static final float LOAD_FACTOR = 0.75f;

  /*
   * Default concurrency level of 16 threads is probably sufficient in most real world deployments. Note that the
   * setting is for updating threads only, thus is concerned only when a tracked class is instantiated the first time.
   * 
   * Impacts memory and cpu usage.
   */
  private static final int CONCURRENCY_LEVEL = 16;

  private static ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<String, AtomicLong>(MAP_SIZE,
      LOAD_FACTOR, CONCURRENCY_LEVEL);

  /*
   * Toggle controlling whether the tracker should track instantiations.
   */
  private static volatile boolean count = false;

  /**
   * Call back invoked by BCI inserted code when a class is instantiated. The class name must be an interned/constant
   * value to avoid leaking!
   * 
   * @param className
   *          name of the class that has just been instantiated.
   */
  public static void constructed(String className) {
    if (!count) {
      return;
    }
    AtomicLong atomicLong = counts.get(className);
    // for most cases the long should exist already.
    if (atomicLong == null) {
      atomicLong = new AtomicLong();
      AtomicLong oldValue = counts.putIfAbsent(className, atomicLong);
      if (oldValue != null) {
        // if the put returned an existing value that one is used.
        atomicLong = oldValue;
      }
    }
    atomicLong.incrementAndGet();
  }

  /**
   * Clears recorded data and starts recording.
   */
  public static void start() {
    counts.clear();
    count = true;
  }

  /**
   * Stops recording.
   */
  public static void stop() {
    count = false;
  }

  /**
   * Builds a human readable list of class names and instantiation counts.
   * 
   * Note: this method will create garbage while building and sorting the top list. The amount of garbage created is
   * dictated by the amount of classes tracked, not by the amount requested.
   * 
   * @param amount
   *          controls how many results are included in the top list. If <= 0 will default to DEFAULT_AMOUNT.
   * @return a newline separated String containing class names and invocation counts.
   */
  public static String buildTopList(final int amount) {
    Set<Entry<String, AtomicLong>> entrySet = counts.entrySet();
    ArrayList<ClassCounter> cc = new ArrayList<ClassCounter>(entrySet.size());

    for (Entry<String, AtomicLong> entry : entrySet) {
      cc.add(new ClassCounter(entry.getKey(), entry.getValue().get()));
    }
    Collections.sort(cc);
    StringBuilder sb = new StringBuilder();
    int max = Math.min(amount <= 0 ? DEFAULT_AMOUNT : amount, cc.size());
    for (int i = 0; i < max; i++) {
      sb.append(cc.get(i).toString());
      sb.append('\n');
    }
    return sb.toString();
  }
}

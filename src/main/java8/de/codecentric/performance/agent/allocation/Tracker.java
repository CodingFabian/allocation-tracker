package de.codecentric.performance.agent.allocation;

import static de.codecentric.performance.agent.allocation.TrackerConfig.CONCURRENCY_LEVEL;
import static de.codecentric.performance.agent.allocation.TrackerConfig.DEFAULT_AMOUNT;
import static de.codecentric.performance.agent.allocation.TrackerConfig.LOAD_FACTOR;
import static de.codecentric.performance.agent.allocation.TrackerConfig.MAP_SIZE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Main class, which is notified by BCI inserted code when an object is constructed. This class keeps a
 * ConcurrentHashMap with class names as keys. This is "leaking" the class name by design, so that the class name string
 * is kept even when the class has been unloaded. For each class name the ConcurrentHashMap will store an LongAdder
 * Instance.
 * 
 * Compatibility: Java 8+
 */
public class Tracker {

  private static ConcurrentHashMap<String, LongAdder> counts = new ConcurrentHashMap<String, LongAdder>(MAP_SIZE,
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
    LongAdder longAdder = counts.get(className);
    // for most cases the long should exist already.
    if (longAdder == null) {
      longAdder = new LongAdder();
      LongAdder oldValue = counts.putIfAbsent(className, longAdder);
      if (oldValue != null) {
        // if the put returned an existing value that one is used.
        longAdder = oldValue;
      }
    }
    longAdder.increment();
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
    Set<Entry<String, LongAdder>> entrySet = counts.entrySet();
    ArrayList<ClassCounter> cc = new ArrayList<ClassCounter>(entrySet.size());

    for (Entry<String, LongAdder> entry : entrySet) {
      cc.add(new ClassCounter(entry.getKey(), entry.getValue().longValue()));
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

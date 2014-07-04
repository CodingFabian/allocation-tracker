package de.codecentric.performance.agent.allocation;

/**
 * Poor mans logging wrapper. The agent does very little logging, so it can easily get lost in complex environments.
 * This class could be changed to write to a dedicated file if desired.
 */
public class AgentLogger {

  public static final boolean LOG_TOP_LIST = true;

  /**
   * Logs the given message to stout and stderr. Reason behind this is that in the past it has proven to be difficult to
   * locate the one or the other file. By writing to both outputs we increase the chance of not missing the agent
   * output.
   * 
   * @param logMessage
   *          message as String
   */
  public static void log(String logMessage) {
    System.out.print("codecentric allocation agent - ");
    System.out.println(logMessage);
    System.err.print("codecentric allocation agent - ");
    System.err.println(logMessage);
  }

}

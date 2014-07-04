package de.codecentric.performance.agent.allocation.mbean;

import de.codecentric.performance.agent.allocation.AgentLogger;
import de.codecentric.performance.agent.allocation.Tracker;

public class Agent implements AgentMBean {

  @Override
  public void start() {
    AgentLogger.log("Agent is now tracking.");
    Tracker.start();
  }

  @Override
  public void stop() {
    AgentLogger.log("Agent is no longer tracking.");
    Tracker.stop();
  }

  @Override
  public String printTop(int amount) {
    String topList = Tracker.buildTopList(amount);
    if (AgentLogger.LOG_TOP_LIST) {
      AgentLogger.log("Agent saw these allocations:");
      AgentLogger.log(topList);
    }
    return topList;
  }

}

package de.codecentric.performance.agent.allocation.mbean;

public interface AgentMBean {

  public void start();

  public void stop();

  public String printTop(int amount);

}

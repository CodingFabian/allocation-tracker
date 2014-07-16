# object allocation tracker

## Purpose and Scope
The purpose of this agent is to fix excessive object allocation issues. While most profilers do offer allocation tracking, they all struggle heavily when the garbage issue is really out of hand. Examples are for example gigabytes of object allocations per second.

This single purpose agent is extremely lightweight and quite optimized (improvement PRs are welcome at any time!).

It is intended to be inserted into production or load test environments and will give you a report of the creation count of all classes matching the configured package prefix.

Usually you will know where you create instances of your classes and you can improve the situation right after the agent tells you what are your most frequently created objects.

The agent does not differentiate between live and garbage objects, issues with live objects can usually be solved with a heap dump, finding out which objects create garbage is usually much harder.

## Limitations
By design you have to give a non empty prefix which limits the classes which are instrumented. You should use your own package only. Trying to track JVM/platform packages is not recommended.

If you create more than `Long.MAX_VALUE` instances of a tracked class the counter will roll over without you noticing it, but that scenario is unlikely. The agent uses `AtomicLong` to track the instantiations for broad JVM support. A special JDK 8 build will use  `LongAdder` which has better performance under contention.

The agent is controlled by a `PlatformMBeanServer` MBean. This might require activation on certain application servers.

For performance reasons the agent does not try to figure out if a class is a superclass or not. Thus superclasses will be included in the report and common base classes will likely appear at the top.

## Build
The agent currently supports JDK 6+7 as well as JDK8.

By default the Maven build will activate the profile `java-6`, which will build an agent that works from JDK 6 onwards.

Build it with `mvn clean package`. 

To build an optimized agent for Java8 (which will use LongAdder as mentioned above) run:
`mvn -P java-8 clean package`

Please always use `clean` with your build commands when building both versions, as they might overwrite / leave behind classes you do not want.

The same profile applies also to the Eclipse project generation. If you switch between both it is recommended to use `eclipse:clean` like this:
`mvn -P java-8 eclipse:clean eclipse:eclipse`
Note that the Eclipse project generation does not know which JDK you use to compile inside Eclipse. Make sure you use the correct one. 


## Usage
Run your application with `-javaagent:/path/to/agent.jar=package.prefix`

E.g if your name is Fabian and you are working on a codecentric project on your Macbook:
`-javaagent:/Users/fabian/work/allocation-tracker/target/allocation-tracker-agent-0.0.1-SNAPSHOT.jar=de.codecentric`

After the agent has printed `codecentric allocation agent - Registered Agent MBean.` the agent is ready to use.
Connect to the JVM with any JMX client and use the `de.codecentric.Agent` MBean to `start` tracking allocations. Use `stop` to stop tracking allocations. `printTop`can be used any time to print the most often created object instances. The parameter will control the amount of output. If the parameter is zero or less, it will default to 100.

## Where are the freaking tests?
Glad that you asked. Please take a look at the source files and then come up with a sensible unit test. If you manage to do that feel free to put up a PR.

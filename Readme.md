# object allocation tracker

## Purpose and Scope
The purpose of this agent is to fix excessive object allocation issues. While most profilers do offer allocation tracking, they all struggle heavily when the garbage issue is really out of hand. Examples are for example gigabytes of object allocations per second.

This single purpose agent is extremely lightweight and quite optimized (improvement PRs are welcome at any time!).

It is intended to be inserted into production or load test environments and will give you a report of the creation count of all classes matching the configured package prefix.

Usually you will know where you create instances of your classes and you can improve the situation right after the agent tells you what are your most frequently created objects.

The agent does not differentiate between live and garbage objects, issues with live objects can usually be solved with a heap dump, finding out which objects create garbage is usually much harder.

## Limitations
By design you have to give a non empty prefix which limits the classes which are instrumented. You should use your own package only. Trying to track JVM/platform packages is not recommended.

If you create more than `Long.MAX_VALUE` instances of a tracked class the counter will roll over without you noticing it, but that scenario is unlikely. The agent uses `AtomicLong` to track the instantiations for broad JVM support. If it proves to be contented, a `LongAdder` might be used in conjunction with Java 8.

The agent is controlled by a `PlatformMBeanServer` MBean. This might require activation on certain application servers.

For performance reasons the agent does not try to figure out if a class is a superclass or not. Thus superclasses will be included in the report and common base classes will likely appear at the top.

## Usage
Build with `mvn package`. Run your application with `-javaagent:/path/to/agent.jar=package.prefix`

E.g if your name is Fabian and you are working on a codecentric project on your Macbook:
`-javaagent:/Users/fabian/work/allocation-tracker/target/allocation-tracker-agent-0.0.1-SNAPSHOT.jar=de.codecentric`

After the agent has printed `codecentric allocation agent - Registered Agent MBean.` the agent is ready to use.
Connect to the JVM with any JMX client and use the `de.codecentric.Agent` MBean to `start` tracking allocations. Use `stop` to stop tracking allocations. `printTop`can be used any time to print the most often created object instances. The parameter will control the amount of output. If the parameter is zero or less, it will default to 100.

## Where are the freaking tests?
Glad that you asked. Please take a look at the source files and then come up with a sensible unit test. If you manage to do that feel free to put up a PR.

## Info on ASM
This agent uses ASM (http://asm.ow2.org/) for bytecode manipulation. It contains a repackaged asm 5.0.3 implementation in the `de.codecentric.performance.asm` package. Besides the package move it is the original ASM code and all of their copyrights apply.
It has been repackaged to avoid conflicts with possible other ASM versions on the classpath.

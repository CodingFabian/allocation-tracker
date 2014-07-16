package de.codecentric.performance.agent.allocation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ClassFileTransformer implementation which will use ASM to visit the bytecode of constructors of classes matching the
 * given prefix. The actual modification of the constructor bytecode is performed by ConstructorVisitor.
 */
public class AllocationTrackerClassFileTransformer implements ClassFileTransformer {

  private String prefix;

  public AllocationTrackerClassFileTransformer(String prefix) {
    this.prefix = prefix;
    if (prefix.startsWith("java")) {
      AgentLogger.log("You are trying to instrument JVM core classes - this might crash the JVM");
    }
  }

  @Override
  public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (className.startsWith(TrackerConfig.AGENT_PACKAGE_PREFIX)) {
      // Safeguard: do not instrument our own classes
      return classfileBuffer;
    }

    if (!className.startsWith(prefix)) {
      return classfileBuffer;
    }

    ClassReader classReader = new ClassReader(classfileBuffer);
    ClassWriter classWriter = new ClassWriter(classReader, 0);
    ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5, classWriter) {

      @Override
      public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, desc, signature, exceptions);
        if (methodName.equals("<init>")) {
          return new ConstructorVisitor(className, methodVisitor);
        }
        return methodVisitor;
      }

    };
    classReader.accept(classVisitor, 0);
    return classWriter.toByteArray();
  }

}

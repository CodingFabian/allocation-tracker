package de.codecentric.performance.agent.allocation;

import de.codecentric.performance.agent.asm.org.objectweb.asm.MethodVisitor;
import de.codecentric.performance.agent.asm.org.objectweb.asm.Opcodes;

/**
 * Changes the bytecode of the visited constructor to call the static tracker. technically could be added to any method.
 */
public class ConstructorVisitor extends MethodVisitor {

  /*
   * reference to the class name of the visited class.
   */
  private String className;

  public ConstructorVisitor(String className, MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
    this.className = className;
  }

  @Override
  public void visitCode() {
    // Pass in the className used by the visitor, which is always the single class name instance from the loaded class.
    // No garbage is produced here.
    super.visitLdcInsn(className);
    super.visitMethodInsn(Opcodes.INVOKESTATIC, TrackerConfig.TRACKER_CLASS, TrackerConfig.TRACKER_CALLBACK,
        TrackerConfig.TRACKER_CALLBACK_SIGNATURE, false);
    super.visitCode();
  }
}

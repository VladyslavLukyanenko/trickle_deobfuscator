package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.InvokeDynamicDeobfInspector;
import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.ObfuscatedStr;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class InvokeDynamicDeobfuscationProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node instanceof InvokeDynamicInsnNode;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ivd = (InvokeDynamicInsnNode) node;
    if (node.getPrevious() instanceof LdcInsnNode s && s.cst instanceof String
      && ivd.bsmArgs.length == 4
      && ivd.bsmArgs[0] instanceof Integer) {
      var ldc = (LdcInsnNode) node.getPrevious();
      var str = (String) ldc.cst;
      var infParam = (int) ivd.bsmArgs[0];
      var obfuscatedClassName = (String) ivd.bsmArgs[1];
      var obfuscatedMethodName = (String) ivd.bsmArgs[2];
      var obfuscatedDescriptor = (String) ivd.bsmArgs[3];
      final String callerClassName = cls.name.replace('/', '.');
      final String calledMethodName = method.name;
      var info = InvokeDynamicDeobfInspector.getCallSite(infParam, obfuscatedClassName, obfuscatedMethodName,
        obfuscatedDescriptor, callerClassName, calledMethodName);

      if (info.getClassName().equals("c") && info.getMethodName().equals("2")) {
        try {
          System.out.println(callerClassName + "." + calledMethodName);
          var deobfuscatedStr = new ObfuscatedStr((String) ldc.cst, 3, callerClassName, calledMethodName);
          ldc.cst = deobfuscatedStr.deobfuscate();
          iterator.remove();

          var nextAfterStr = iterator.next();
          if (nextAfterStr.getOpcode() == Opcodes.CHECKCAST) {
            iterator.remove();
            iterator.next();
          } else {
            iterator.previous();
          }

          return true;
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("WARN: failed to deobfuscate: " + str);
        }
      }
    }


    if (ivd.bsm.getOwner().equals("2")
      && ivd.bsm.getName().equals("b")) {
      var infParam = (int) ivd.bsmArgs[0];
      var obfuscatedClassName = (String) ivd.bsmArgs[1];
      var obfuscatedMethodName = (String) ivd.bsmArgs[2];
      var obfuscatedDescriptor = (String) ivd.bsmArgs[3];
      final String callerClassName = cls.name.replace('/', '.');
      final String calledMethodName = method.name;
      var info = InvokeDynamicDeobfInspector.getCallSite(infParam, obfuscatedClassName, obfuscatedMethodName,
        obfuscatedDescriptor, callerClassName, calledMethodName);

      var opcode = info.isVirtualCall() ? INVOKEVIRTUAL : INVOKESTATIC;
      final MethodInsnNode callInsn = new MethodInsnNode(opcode, info.getClassName(), info.getMethodName(), info.getMethodDescriptor(), false);
      iterator.remove();
      iterator.add(callInsn);
      var next = iterator.next();
      if (next.getOpcode() == Opcodes.CHECKCAST) {
        iterator.remove();
        iterator.next();
      }

      return true;
    }
    /*if (ivd.bsm.getOwner().equals("makeConcatWithConstants")
      && ivd.bsm.getName().equals("java/lang/invoke/StringConcatFactory")) {
      var str = (String) ivd.bsmArgs[0];
//      var obfuscatedClassName = (String) ivd.bsmArgs[1];
//      var obfuscatedMethodName = (String) ivd.bsmArgs[2];
//      var obfuscatedDescriptor = (String) ivd.bsmArgs[3];
//      final String callerClassName = cls.name.replace('/', '.');
//      final String calledMethodName = method.name;
//      var info = InvokeDynamicDeobfInspector.getCallSite(infParam, obfuscatedClassName, obfuscatedMethodName,
//        obfuscatedDescriptor, callerClassName, calledMethodName);
//
//      var opcode = info.isVirtualCall() ? INVOKEVIRTUAL : INVOKESTATIC;
//      final MethodInsnNode callInsn = new MethodInsnNode(opcode, info.getClassName(), info.getMethodName(), info.getMethodDescriptor(), false);
//      iterator.remove();
//      iterator.add(callInsn);
//      var next = iterator.next();
//      if (next.getOpcode() == Opcodes.CHECKCAST) {
//        iterator.remove();
//        iterator.next();
//      }

      return true;
    }*/

    return false;
  }
}

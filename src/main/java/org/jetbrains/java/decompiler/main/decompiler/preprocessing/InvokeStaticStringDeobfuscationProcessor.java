package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.InvokeDynamicDeobfInspector;
import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.ObfuscatedStr;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class InvokeStaticStringDeobfuscationProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node instanceof MethodInsnNode;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var methodInsnNode = (MethodInsnNode) node;
    if (
      methodInsnNode.getOpcode() != INVOKESTATIC || !methodInsnNode.name.equals("2")
      || !methodInsnNode.owner.equals("c")
      || !(node.getPrevious() instanceof LdcInsnNode ldc)
    ) {
      return false;
    }
    var str = (String) ldc.cst;
    var next = (MethodInsnNode) ldc.getNext();

    var desc = MethodDescriptor.parseDescriptor(next.desc);
    if (desc.params.length != 1) {
      throw new UnsupportedOperationException("Call with specific deobf param is not supported");
    }

    try {
      final String callerClassName = cls.name.replace('/', '.');
      final String calledMethodName = method.name;
      var deobfuscatedStr = new ObfuscatedStr(str, 3, callerClassName, calledMethodName);
      ldc.cst = deobfuscatedStr.deobfuscate();
      iterator.remove();

      if (iterator.hasNext()) {
        var nextAfterStr = iterator.next();
        if (nextAfterStr.getOpcode() == Opcodes.CHECKCAST) {
          iterator.remove();
          iterator.next();
        } else {
          iterator.previous();
        }
      }

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("WARN: failed to deobfuscate: " + str);
      return false;
    }
//      var infParam = (int) ivd.bsmArgs[0];
//      var obfuscatedClassName = (String) ivd.bsmArgs[1];
//      var obfuscatedMethodName = (String) ivd.bsmArgs[2];
//      var obfuscatedDescriptor = (String) ivd.bsmArgs[3];
//      final String callerClassName = cls.name.replace('/', '.');
//      final String calledMethodName = method.name;
//      var info = InvokeDynamicDeobfInspector.getCallSite(infParam, obfuscatedClassName, obfuscatedMethodName,
//        obfuscatedDescriptor, callerClassName, calledMethodName);
//
//      if (!info.getClassName().equals("c") || !info.getMethodName().equals("2")) {
//        var opcode = info.isVirtualCall() ? INVOKEVIRTUAL : INVOKESTATIC;
//        final MethodInsnNode callInsn = new MethodInsnNode(opcode, info.getClassName(), info.getMethodName(), info.getMethodDescriptor(), false);
//        iterator.next();
//        iterator.remove();
//        iterator.add(callInsn);
//        var nextAfterCall = iterator.next();
//        if (nextAfterCall.getOpcode() == Opcodes.CHECKCAST) {
//          iterator.remove();
//          iterator.next();
//        }
//
//        return true;
//      }
//
//      try {
//        System.out.println(callerClassName + "." + calledMethodName);
//        // todo: add support for <init> and <cinit>
//        var deobfuscatedStr = new ObfuscatedStr((String) ldc.cst, 3, callerClassName, calledMethodName);
//        ldc.cst = deobfuscatedStr.deobfuscate();
//        iterator.next();
//        iterator.remove();
//
//        var nextAfterStr = iterator.next();
//        if (nextAfterStr.getOpcode() == Opcodes.CHECKCAST) {
//          iterator.remove();
//          iterator.next();
//        } else {
//          iterator.previous();
//        }
//
//        return true;
//      } catch (Exception e) {
//        e.printStackTrace();
//        System.err.println("WARN: failed to deobfuscate: " + str);
//      }

  }
}

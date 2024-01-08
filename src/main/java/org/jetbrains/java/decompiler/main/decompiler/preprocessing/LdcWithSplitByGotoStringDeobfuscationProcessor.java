package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.ObfuscatedStr;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class LdcWithSplitByGotoStringDeobfuscationProcessor implements InstructionProcessor {
  private static List<String> processed = new ArrayList<>();
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node instanceof LdcInsnNode ldc && ldc.cst instanceof String
      && !processed.contains(ldc.cst)
      && !(node.getNext() instanceof MethodInsnNode);
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ldc = (LdcInsnNode) node;
    var str = (String) ldc.cst;
    boolean isObfuscated = false;
    var bytes = str.getBytes();
    for (int i = 0; i < str.length(); i++) {
      byte curr = bytes[i];
      if (curr <= 0) {
        isObfuscated = true;
        break;
      }
    }

    if (!isObfuscated) {
      return false;
    }

    try {
      final String callerClassName = cls.name.replace('/', '.');
      final String calledMethodName = method.name;
      var deobfuscatedStr = new ObfuscatedStr(str, 3, callerClassName, calledMethodName);
      final var cst = deobfuscatedStr.deobfuscate();
      ldc.cst = cst;
      processed.add(cst);

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

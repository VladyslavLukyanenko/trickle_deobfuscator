package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.InvokeDynamicDeobfInspector;
import org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation.ObfuscatedStr;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.DynamicClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ARETURN;

public class DynCallStringDeobfuscationProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node instanceof LdcInsnNode s && s.cst instanceof String
      && node.getNext() instanceof InvokeDynamicInsnNode c
      && c.bsmArgs.length == 4
      && c.bsmArgs[0] instanceof Integer;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ldc = (LdcInsnNode) node;
    var str = (String) ldc.cst;

    var next = ldc.getNext();

    if (next instanceof InvokeDynamicInsnNode ivd) {
      var infParam = (int) ivd.bsmArgs[0];
      var obfuscatedClassName = (String) ivd.bsmArgs[1];
      var obfuscatedMethodName = (String) ivd.bsmArgs[2];
      var obfuscatedDescriptor = (String) ivd.bsmArgs[3];
      final String callerClassName = cls.name.replace('/', '.');
      final String calledMethodName = method.name;
      var info = InvokeDynamicDeobfInspector.getCallSite(infParam, obfuscatedClassName, obfuscatedMethodName,
        obfuscatedDescriptor, callerClassName, calledMethodName);

      if (!info.getClassName().equals("c") || !info.getMethodName().equals("2")) {
        var opcode = info.isVirtualCall() ? INVOKEVIRTUAL : INVOKESTATIC;
        final MethodInsnNode callInsn = new MethodInsnNode(opcode, info.getClassName(), info.getMethodName(), info.getMethodDescriptor(), false);
        iterator.next();
        iterator.remove();
        iterator.add(callInsn);
        var nextAfterCall = iterator.next();
        if (nextAfterCall.getOpcode() == Opcodes.CHECKCAST) {
          iterator.remove();
          iterator.next();
        }

        return true;
      }

      try {
        System.out.println(callerClassName + "." + calledMethodName);
        // todo: add support for <init> and <cinit>
        var deobfuscatedStr = new ObfuscatedStr((String) ldc.cst, 3, callerClassName, calledMethodName);
        ldc.cst = deobfuscatedStr.deobfuscate();
        iterator.next();
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
    return false;
  }
}

package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrSourceBytesCollectorProcessor implements InstructionProcessor {
  private static final Map<String, byte[]> SRC_BYTES = new ConcurrentHashMap<>();

  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node instanceof LdcInsnNode l && l.cst instanceof String;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var str = ((String) ((LdcInsnNode) node).cst);
    SRC_BYTES.put(str, str.getBytes());
    return false;
  }
}

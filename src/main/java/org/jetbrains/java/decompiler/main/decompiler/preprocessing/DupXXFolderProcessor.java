package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class DupXXFolderProcessor implements InstructionProcessor {
  private static final int MAX = 4;

  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    var diff = Math.abs(node.getOpcode() - Opcodes.DUP2_X2);
    if (node.getOpcode() > Opcodes.DUP2_X2 || diff > 2) {
      return false;
    }

    var nodes = new ArrayList<LdcInsnNode>();
    AbstractInsnNode last = node;
    for (int i = 0; i < MAX - diff /* Max 4 words */; i++) {
      var n = last.getPrevious();
      if (n == null) {
        break;
      }
      if (n instanceof LdcInsnNode) {
        var l = (LdcInsnNode) n;
        if (!(l.cst instanceof String) && !(l.cst instanceof Double) && !(l.cst instanceof Long)) {
          nodes.add(l);
          last = l;
        }
      } else {
        break;
      }
    }

    return nodes.size() == MAX - diff;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var diff = Math.abs(node.getOpcode() - Opcodes.DUP2_X2);
    var nodes = new LinkedList<LdcInsnNode>();
    AbstractInsnNode last = node;
    for (int i = 0; i < MAX - diff; i++) {
      var n = last.getPrevious();
      if (n instanceof LdcInsnNode) {
        var l = (LdcInsnNode) n;
        if (!(l.cst instanceof String) && !(l.cst instanceof Double) && !(l.cst instanceof Long)) {
          nodes.addFirst(new LdcInsnNode(l.cst));
          last = l;
        }
      } else {
        break;
      }
    }

    for (var n : nodes) {
      method.instructions.insertBefore(node, n);
    }

    iterator.remove();
    return true;
  }
}

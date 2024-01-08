package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class IConstProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    final int opcode = node.getOpcode();
    return opcode == Opcodes.ICONST_M1
        || opcode == Opcodes.ICONST_0
        || opcode == Opcodes.ICONST_1
        || opcode == Opcodes.ICONST_2
        || opcode == Opcodes.ICONST_3
        || opcode == Opcodes.ICONST_4
        || opcode == Opcodes.ICONST_5;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    final int opcode = node.getOpcode();
    int value = -1;
    switch (opcode) {
      case Opcodes.ICONST_M1: {
        value = -1;
        break;
      }
      case Opcodes.ICONST_0: {
        value = 0;
        break;
      }
      case Opcodes.ICONST_1: {
        value = 1;
        break;
      }
      case Opcodes.ICONST_2: {
        value = 2;
        break;
      }
      case Opcodes.ICONST_3: {
        value = 3;
        break;
      }
      case Opcodes.ICONST_4: {
        value = 4;
        break;
      }
      case Opcodes.ICONST_5: {
        value = 5;
        break;
      }
    }
    iterator.set(new LdcInsnNode(value));
//    method.instructions.insertBefore(node, new LdcInsnNode(value));
//    method.instructions.remove(node);
//    iterator.remove();
    return true;
  }
}

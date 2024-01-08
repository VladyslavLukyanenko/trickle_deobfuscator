package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class IXorFolderProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node.getOpcode() == Opcodes.IXOR
        && node.getPrevious() instanceof LdcInsnNode v
        && v.cst instanceof Integer;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ldcLast = (LdcInsnNode) node.getPrevious();
    ldcLast.cst = ~(Integer)ldcLast.cst;
    iterator.remove();
    return true;
  }
}


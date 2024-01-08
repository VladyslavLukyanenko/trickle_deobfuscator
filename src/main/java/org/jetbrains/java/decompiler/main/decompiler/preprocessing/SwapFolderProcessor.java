package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class SwapFolderProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node.getOpcode() == Opcodes.SWAP
        && node.getPrevious() instanceof LdcInsnNode last
        && !(last.cst instanceof Long)
        && !(last.cst instanceof Double)

        && node.getPrevious().getPrevious() instanceof LdcInsnNode first
        && !(first.cst instanceof Long)
        && !(first.cst instanceof Double);
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ldcLast = (LdcInsnNode) node.getPrevious();
    var ldcFirst = (LdcInsnNode) ldcLast.getPrevious();
    Object tmp = ldcFirst.cst;
    ldcFirst.cst = ldcLast.cst;
    ldcLast.cst = tmp;
    iterator.remove();
    return true;
  }
}

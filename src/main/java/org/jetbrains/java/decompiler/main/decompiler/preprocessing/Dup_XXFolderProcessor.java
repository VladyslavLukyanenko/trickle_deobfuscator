package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class Dup_XXFolderProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return (node.getOpcode() == Opcodes.DUP || node.getOpcode() == Opcodes.DUP_X1 || node.getOpcode() == Opcodes.DUP_X2)
        && node.getPrevious() instanceof LdcInsnNode l
        && !(l.cst instanceof Long)
        && !(l.cst instanceof Double);
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    if (node.getOpcode() == Opcodes.DUP) {
      var ldcLast = (LdcInsnNode) node.getPrevious();
      method.instructions.insertBefore(node, new LdcInsnNode(ldcLast.cst));
    } else if (node.getOpcode() == Opcodes.DUP_X1) {
      var ldcLast = (LdcInsnNode) node.getPrevious();
      var ldcFirst = (LdcInsnNode) ldcLast.getPrevious();
      method.instructions.insertBefore(node, new LdcInsnNode(ldcFirst.cst));
      method.instructions.insertBefore(node, new LdcInsnNode(ldcLast.cst));
    }
    {
      var ldcLast = node.getPrevious();
      var ldcMiddle = (LdcInsnNode) ldcLast.getPrevious();
      var ldcFirst = (LdcInsnNode) ldcMiddle.getPrevious();
      if (ldcMiddle.cst instanceof Long || ldcMiddle.cst instanceof Double) {
        method.instructions.insertBefore(node, new LdcInsnNode(ldcFirst.cst));
        method.instructions.insertBefore(node, new LdcInsnNode(ldcMiddle.cst));
      } else {
        method.instructions.insertBefore(node, new LdcInsnNode(((LdcInsnNode) ldcLast).cst));
        method.instructions.insertBefore(node, new LdcInsnNode(ldcMiddle.cst));
        method.instructions.insertBefore(node, new LdcInsnNode(ldcFirst.cst));
      }
    }

    iterator.remove();
    return true;
  }
}

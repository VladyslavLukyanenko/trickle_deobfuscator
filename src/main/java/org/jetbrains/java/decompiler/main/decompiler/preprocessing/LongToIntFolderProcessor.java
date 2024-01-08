package org.jetbrains.java.decompiler.main.decompiler.preprocessing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class LongToIntFolderProcessor implements InstructionProcessor {
  @Override
  public boolean canProcess(AbstractInsnNode node, MethodNode method, ClassNode cls) {
    return node.getOpcode() == Opcodes.L2I && node.getPrevious() instanceof LdcInsnNode;
  }

  @Override
  public boolean process(AbstractInsnNode node, MethodNode method, ClassNode cls, ListIterator<AbstractInsnNode> iterator) {
    var ldc = (LdcInsnNode) node.getPrevious();
    ldc.cst = ((Long)ldc.cst).intValue();
    iterator.remove();
//    method.instructions.remove(node);
    return true;
  }
}

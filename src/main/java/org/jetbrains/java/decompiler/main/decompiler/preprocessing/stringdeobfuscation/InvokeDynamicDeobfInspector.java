package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;


public class InvokeDynamicDeobfInspector {

  public static CallSiteInfo getCallSite(int obfParam, String obfuscatedClassName, String obfuscatedMethodName,
                                         String methodDescriptorString, String callerClassName, String callerMethodName)/* throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException*/ {
    final String className = StrDeobfuscatr.deobfuscateStr(obfuscatedClassName, callerClassName, callerMethodName);
//    var targetCls = Class.forName(className);
    var methodName = StrDeobfuscatr.deobfuscateStr(obfuscatedMethodName, callerClassName, callerMethodName);
    methodDescriptorString = StrDeobfuscatr.deobfuscateStr(methodDescriptorString, callerClassName, callerMethodName);
//    MethodType methodDescriptor = MethodType.fromMethodDescriptorString(methodDescriptorString, _2.class.getClassLoader());

//    MethodHandle methodHandle2;
//    MethodHandle methodHandle;
    if (obfParam != 184) {
      if (obfParam == 182 || obfParam == 185) {
        return new CallSiteInfo(className, methodName, methodDescriptorString, true);
        /*methodHandle2 = lookup.findVirtual(targetCls, methodName, methodDescriptor);
        methodHandle = methodHandle2;
        methodHandle2 = methodHandle.asType(methodType);
        return new ConstantCallSite(methodHandle2);*/
      }

      throw null;
    } else {
      return new CallSiteInfo(className, methodName, methodDescriptorString, false);
      /*methodHandle = lookup.findStatic(targetCls, methodName, methodDescriptor);

      methodHandle2 = methodHandle.asType(methodType);
      return new ConstantCallSite(methodHandle2);*/
    }
  }
}

package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;

public class CallSiteInfo {
  private final String className;
  private final String methodName;
  private final String methodDescriptor;
  private final boolean isVirtualCall;

  public CallSiteInfo(String className, String methodName, String methodDescriptor, boolean isVirtualCall) {
    this.className = className;
    this.methodName = methodName;
    this.methodDescriptor = methodDescriptor;
    this.isVirtualCall = isVirtualCall;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getMethodDescriptor() {
    return methodDescriptor;
  }

  public boolean isVirtualCall() {
    return isVirtualCall;
  }

  @Override
  public String toString() {
    return "CallInfo{" +
      "className='" + className + '\'' +
      ", methodName='" + methodName + '\'' +
      ", methodDescriptor='" + methodDescriptor + '\'' +
      ", isVirtualCall=" + isVirtualCall +
      '}';
  }
}

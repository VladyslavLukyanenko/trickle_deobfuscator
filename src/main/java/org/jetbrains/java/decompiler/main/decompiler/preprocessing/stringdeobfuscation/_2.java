package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;

public class _2 {

  public static Object b(Lookup lookup, String var1, MethodType methodType, int obfParam, String var4, String methodName, String methodDescriptorString) throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
    Class targetCls = null;
    MethodType methodDescriptor = null;
    MethodHandle methodHandle2 = null;
    int step = 7;

    while (true) {
      label184:
      while (true) {
        MethodHandle methodHandle;
        label180:
        while (true) {
          while (true) {
            while (true) {
              label174:
              while (true) {
                label172:
                while (true) {
                  label170:
                  while (true) {
                    while (true) {
                      while (true) {
                        while (true) {
                          label158:
                          while (true) {
                            label152:
                            while (true) {
                              label216:
                              {
                                switch (step) {
                                  case 0:
                                    if (obfParam != 184) {
                                      step = 3;
                                    } else {
                                      step = 9;
                                    }
                                    continue;
                                  case 1:
                                    methodDescriptorString = a(methodDescriptorString);
                                    step = 6;
                                    continue;
                                  case 3:
                                    if (obfParam == 182) {
                                      break label184;
                                    }

                                    step = 8;
                                    continue;
//                                  case 5:
//                                  case 41:
//                                  case 71:
//                                  case 101:
//                                    Object var10001 = null;
//
//                                    step = 4;
//                                    continue;
                                  case 6:
                                    methodDescriptor = MethodType.fromMethodDescriptorString(methodDescriptorString, _2.class.getClassLoader());
                                    step = 0;
                                    continue;
                                  case 7:
                                    break label170;
                                  case 8:
                                    if (obfParam == 185) {
                                      break label184;
                                    }

                                    step = 11;
                                    continue;
                                  case 9:
                                    methodHandle = lookup.findStatic(targetCls, methodName, methodDescriptor);
                                    break label180;
                                  case 10:
                                    methodName = a(methodName);
                                    step = 1;
                                    continue;
                                  case 11:
                                    throw null;
                                  case 12:
                                    return new ConstantCallSite(methodHandle2);
                                  case 14:
                                  case 35:
                                  case 50:
                                  case 65:
                                  case 80:
                                  case 95:
                                  case 110:
                                  case 125:
                                    methodHandle = methodHandle2;
                                    break label180;
                                  case 15:
                                    break label172;
                                  case 16:
                                  case 32:
                                  case 62:
                                  case 92:
                                  case 122:
                                    break label174;
                                  case 17:
                                    methodDescriptorString = a("s.|\ufffdM\ufffdJ\ufffd\ufffdI\ufffd\ufffdM");
                                    break label152;
                                  case 18:
                                    break label158;
                                  case 19:
                                    break label152;
                                  case 20:
                                    break label158;
                                  case 21:
                                  case 51:
                                  case 81:
                                  case 111:
                                    break label216;
                                  case 22:
                                  case 27:
                                  case 52:
                                  case 57:
                                  case 82:
                                  case 87:
                                  case 112:
                                  case 117:
                                    if (_0.g >= 0) {
                                      break label174;
                                    }
                                    break label216;
                                  case 23:
                                  case 53:
                                  case 83:
                                  case 113:
                                    break;
                                  case 25:
                                  case 55:
                                  case 85:
                                  case 115:
                                    while (true) {
                                      if (_0.a == 0) {
                                        continue;
                                      }
                                      break;
                                    }
                                  case 24:
                                  case 54:
                                  case 84:
                                  case 114:
                                    if (_0.e != 0) {
                                    }
                                    break;
                                  case 26:
                                  case 56:
                                  case 86:
                                  case 116:
                                    if (_0.o > 0) {
                                      if (_0.h == 0) {
                                        break label174;
                                      }
                                      break label170;
                                    }
                                    break label158;
                                  case 28:
                                  case 58:
                                  case 88:
                                  case 118:
                                    if (_0.h == 0) {
                                      break label174;
                                    }
                                    break label170;
                                }

                                if (_0._5 > 0) {
                                  break;
                                }
                              }

                              while (_0._0 == 0) {
                                if (_0.g >= 0) {
                                  break label174;
                                }
                              }
                            }

//                                                        targetCls.instanceof();
                            break;
                          }

//                                                    targetCls = Class.forName(step);
                          break label172;
                        }
                      }
                    }
                  }

                  targetCls = Class.forName(var4 = a(var4));
                  step = 10;
                }

                step = 10;
              }

              targetCls = Class.forName("sun.misc.Unsafe");
              step = 3;
            }
          }
        }

        methodHandle2 = methodHandle.asType(methodType);
        step = 12;
      }

      methodHandle2 = lookup.findVirtual(targetCls, methodName, methodDescriptor);
      step = 14;
    }
  }

  public static String a(String srcString) {
    char[] srcStrChars = null;
    char[] deobfStrChars = null;
    Thread currThread = null;
    StackTraceElement stack = null;
    int callerClassHashCode = -1;
    int callerMethodHashCode = -1;
    int currCharIx = -1;
    int step = 7;
    StackTraceElement[] stackTrace = null;
    int traceIx = 2;

    while (true) {
      int deobfuscatedChar;
      label101:
      while (true) {
        label99:
        while (true) {
          while (true) {
            while (true) {
              label93:
              while (true) {
                boolean var10001;
                label91:
                while (true) {
                  switch (step) {
                    case 7:
                      srcStrChars = srcString.toCharArray();
                      /*step = 9;
                      break;
                    case 9:*/
                      deobfStrChars = new char[srcStrChars.length];
                      /*step = 6;
                      break;
                    case 6:*/
                      currThread = Thread.currentThread();
                      /*step = 8;
                      break;
                    case 8:
                      try {*/
                      stackTrace = currThread.getStackTrace();
                      traceIx = 2;
                      /*} catch (IllegalMonitorStateException var13) {
                        var10001 = false;
                        break label91;
                      }*/
                    case 5:
                      try {
                        callerClassHashCode = "io.trickle.App".hashCode(); // stack.getClassName().hashCode();
                        step = 11;
                        break;
                      } catch (IllegalMonitorStateException var12) {
                        var10001 = false;
                        break label91;
                      }
                    case 11:
                      callerMethodHashCode = "main".hashCode(); // stack.getMethodName().hashCode();
                      step = 10;
                      break;
                    case 10:
                      currCharIx = 0;
                      step = 12;
                      break;
                    case 12:
                      break label99;
                    case 0:
                      deobfuscatedChar = srcStrChars[currCharIx] ^ 2;
                      break label101;
                    case 1:
                      deobfuscatedChar = srcStrChars[currCharIx] ^ callerClassHashCode;
                      break label101;
                    case 2:
                      deobfuscatedChar = srcStrChars[currCharIx] ^ callerMethodHashCode;
                      break label101;
                    case 3:
                      deobfuscatedChar = srcStrChars[currCharIx] ^ callerClassHashCode + callerMethodHashCode;
                      break label101;
                    case 4:
                      deobfuscatedChar = srcStrChars[currCharIx] ^ currCharIx;
                      break label101;
                    case 13:
                      step = currCharIx % 5;
                      break;
                    case 14:
                      step = 17;
                      break;
                    case 15:
                      throw null;
                    case 16:
                      step = 18;
                      break;
                    case 17:
                      ++currCharIx;
                      break label99;
                    case 18:
                      step = 14;
                      break;
                    default:
                      step = 15;
                  }
                }

                while (true) {
                  while (true) {
                    StackTraceElement traceElement = stackTrace[++traceIx];
                    if (!traceElement.getClassName().startsWith("java.lang.")) {
                      stack = traceElement;
                      step = 5;
                      continue label93;
                    }

                    try {
                      ;
                    } catch (IllegalMonitorStateException var11) {
                      var10001 = false;
                    }
                  }
                }
              }
            }
          }
        }

        if (currCharIx >= deobfStrChars.length) {
          return new String(srcStrChars);
        }

        step = 13;
      }

      srcStrChars[currCharIx] = (char) deobfuscatedChar;
      step = 17;
    }
  }

//
//    private static String a(String var0) {
//        char[] var1 = null;
//        char[] var2 = null;
//        Thread var3 = null;
//        StackTraceElement var4 = null;
//        int var5 = -1;
//        int var6 = -1;
//        int var7 = -1;
//        int var8 = 7;
//        StackTraceElement[] var9 = null;
//        int var10 = 2;
//
//        while(true) {
//            while(true) {
//                while(true) {
//                    while(true) {
//                        while(true) {
//                            while(true) {
//                                int var14;
//                                label101:
//                                while(true) {
//                                    label99:
//                                    while(true) {
//                                        while(true) {
//                                            while(true) {
//                                                label93:
//                                                while(true) {
//                                                    boolean var10001;
//                                                    label91:
//                                                    while(true) {
//                                                        switch(var8) {
//                                                            case 0:
//                                                                var14 = var1[var7] ^ 2;
//                                                                break label101;
//                                                            case 1:
//                                                                var14 = var1[var7] ^ var5;
//                                                                break label101;
//                                                            case 2:
//                                                                var14 = var1[var7] ^ var6;
//                                                                break label101;
//                                                            case 3:
//                                                                var14 = var1[var7] ^ var5 + var6;
//                                                                break label101;
//                                                            case 4:
//                                                                var14 = var1[var7] ^ var7;
//                                                                break label101;
//                                                            case 6:
//                                                                var3 = Thread.currentThread();
//                                                                var8 = 8;
//                                                                break;
//                                                            case 7:
//                                                                var1 = var0.toCharArray();
//                                                                var8 = 9;
//                                                                break;
//                                                            case 8:
//                                                                try {
//                                                                    var9 = var3.getStackTrace();
//                                                                    var10 = 2;
//                                                                } catch (IllegalMonitorStateException var13) {
//                                                                    var10001 = false;
//                                                                    break label91;
//                                                                }
//                                                            case 5:
//                                                                try {
//                                                                    var5 = var4.getClassName().hashCode();
//                                                                    var8 = 11;
//                                                                    break;
//                                                                } catch (IllegalMonitorStateException var12) {
//                                                                    var10001 = false;
//                                                                    break label91;
//                                                                }
//                                                            case 9:
//                                                                var2 = new char[var1.length];
//                                                                var8 = 6;
//                                                                break;
//                                                            case 10:
//                                                                var7 = 0;
//                                                                var8 = 12;
//                                                                break;
//                                                            case 11:
//                                                                var6 = var4.getMethodName().hashCode();
//                                                                var8 = 10;
//                                                                break;
//                                                            case 12:
//                                                                break label99;
//                                                            case 13:
//                                                                var8 = var7 % 5;
//                                                                break;
//                                                            case 14:
//                                                                var8 = 17;
//                                                                break;
//                                                            case 15:
//                                                                throw null;
//                                                            case 16:
//                                                                var8 = 18;
//                                                                break;
//                                                            case 17:
//                                                                ++var7;
//                                                                break label99;
//                                                            case 18:
//                                                                var8 = 14;
//                                                                break;
//                                                            default:
//                                                                var8 = 15;
//                                                        }
//                                                    }
//
//                                                    while(true) {
//                                                        while(true) {
//                                                            StackTraceElement var10000 = var9[++var10];
//                                                            if (!var10000.getClassName().startsWith("java.lang.")) {
//                                                                var4 = var10000;
//                                                                var8 = 5;
//                                                                continue label93;
//                                                            }
//
//                                                            try {
//                                                                ;
//                                                            } catch (IllegalMonitorStateException var11) {
//                                                                var10001 = false;
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if (var7 >= var2.length) {
//                                        return new String(var1);
//                                    }
//
//                                    var8 = 13;
//                                }
//
//                                var1[var7] = (char)var14;
//                                var8 = 16;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}

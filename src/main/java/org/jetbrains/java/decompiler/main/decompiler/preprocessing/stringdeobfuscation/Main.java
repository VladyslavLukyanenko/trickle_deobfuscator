package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;

public class Main {
  public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IOException, InvocationTargetException {
// cls: io.trickle.App
// method: main
// 184
// "a"
// "0"
    var obfuscatedStrBytes = new byte[]{
        // obfuscatedStr "Starting Trickle v%d.%d.%d%n"
        0, 83, // len
        -24, -79, -104, -32, -89, -122, -22, -119, -112, -18, -100, -123, -24, -123, -100, -32, -86, -86, -19,
        -120, -127, -21, -87, -98, -29, -118, -68, -22, -77, -81, -23, -104, -100, -30, -123, -118, -17, -110,
        -75, -18, -108, -80, -20, -123, -105, -25, -71, -99, -29, -66, -66, -17, -93, -77, -27, -69, -116, -21,
        -78, -88, -32, -94, -124, -22, -109, -91, -19, -110, -84, -30, -101, -72, -24, -124, -98, -44, -93, -24,
        -93, -111, -26, -86, -124,


        //deobfuscationArgBytes "(Ljava/lang/String;)Ljava/lang/String;"
        0, 76, // len
        42, -19, -112, -101, -41, -109, -19, -87, -79, 114, 99, -19, -111, -72, -41, -107, -19, -87, -79, 103,
        101, -19, -111, -72, -41, -86, -19, -87, -92, 124, 107, -19, -112, -71, -41, -98, -19, -88, -85, 58,
        78, -19, -112, -67, -41, -104, -19, -87, -90, 121, 45, -19, -112, -69, -41, -104, -19, -87, -66, 122,
        45, -19, -112, -124, -41, -115, -19, -87, -94, 75, 108, -19, -112, -80, -42, -126
    };
   /* var obfuscatedStrBytes = new byte[]{
        // obfuscatedStr
        0, 62, // len
        42, -19, -112, -101, -32, -69, -125, -18, -115, -95, 114, 99, -19, -111, -72, -32, -69, -102, -18, -115, -91,
        106, 119, -19, -112, -91, -32, -69, -128, -18, -115, -76, 119, 45, -19, -112, -121, -32, -69, -101, -18, -115,
        -81, 101, 107, -19, -112, -77, -32, -69, -116, -18, -115, -78, 35, 75, -19, -111, -66, -32, -69, -96,


        //callMethodSignature
        0, 48, // len
        104, -19, -112, -74, -32, -69, -97, -18, -115, -95, 42, 113, -19, -112, -78, -32, -69, -118, -18, -115, -75,
        123, 107, -19, -112, -93, -32, -69, -112, -18, -116, -82, 93, 103, -19, -112, -76, -32, -69, -100, -18, -115,
        -78, 122, 118, -19, -112, -82,
    };*/

    var in = new DataInputStream(new ByteArrayInputStream(obfuscatedStrBytes));
    var obfuscatedStr = in.readUTF();
    var callMethodSignature = in.readUTF();
    final String methodName = "main"; /*"<clinit>";*/
    final String className = "io.trickle.App";
    var s = new ObfuscatedStr(obfuscatedStr, 3, className, methodName);
    var ds = s.deobfuscate();
    var argRes = StrDeobfuscatr.deobfuscateStr(callMethodSignature, className, methodName);

    final MethodType mtype = MethodType.fromMethodDescriptorString("(Ljava/lang/Object;)Ljava/lang/Object;", Main.class.getClassLoader());
    final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    var r = InvokeDynamicDeobfInspector.getCallSite(/*lookup, "b", mtype, */184, "a", "0", callMethodSignature, className, methodName);
  }
}
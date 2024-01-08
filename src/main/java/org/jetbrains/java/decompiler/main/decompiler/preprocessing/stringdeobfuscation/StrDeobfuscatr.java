package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;

class StrDeobfuscatr {
  public static String deobfuscateStr(String srcString, String className, String methodName) {
    int currCharIx = -1;
    int step;

    char[] srcStrChars = srcString.toCharArray();
    char[] deobfStrChars = new char[srcStrChars.length];
    int callerClassHashCode = className.hashCode();
    int callerMethodHashCode = methodName.hashCode();

    int deobfuscatedChar;
    while (true) {
      ++currCharIx;

      if (currCharIx >= deobfStrChars.length) {
        return new String(srcStrChars);
      }

      step = currCharIx % 5;
      deobfuscatedChar = switch (step) {
        case 0 -> srcStrChars[currCharIx] ^ 2;
        case 1 -> srcStrChars[currCharIx] ^ callerClassHashCode;
        case 2 -> srcStrChars[currCharIx] ^ callerMethodHashCode;
        case 3 -> srcStrChars[currCharIx] ^ callerClassHashCode + callerMethodHashCode;
        case 4 -> srcStrChars[currCharIx] ^ currCharIx;
        default -> throw null;
      };

      srcStrChars[currCharIx] = (char) deobfuscatedChar;
    }
  }
}

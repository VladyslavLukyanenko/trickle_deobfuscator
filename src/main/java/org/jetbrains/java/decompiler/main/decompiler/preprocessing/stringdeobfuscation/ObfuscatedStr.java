package org.jetbrains.java.decompiler.main.decompiler.preprocessing.stringdeobfuscation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ObfuscatedStr {
  private static int CLASSES_COUNT;

  private static int[] aiooi1iojionlknzjsdnfdas;
  private static Map<String, String> deobfscatedCache;

  private final String obfuscatedSourceStr;

  private char[] obfuscatedChars = null;
  private static int magicConst;
  private char[] deobfuscatedChars = null;
  private int currCharIx = -1;
  private int classHash = -1;
  private int methodHash = -1;

  public static void setMagicNum(int num, int[] numbersHolder) {
    magicConst = num;
    CLASSES_COUNT = numbersHolder.length;
    deobfscatedCache = new HashMap<>(CLASSES_COUNT);
    aiooi1iojionlknzjsdnfdas = Arrays.copyOf(numbersHolder, CLASSES_COUNT);
  }

  {
    classHash = 0;
    methodHash = 0;
    obfuscatedChars = null;
    deobfuscatedChars = null;
    currCharIx = 0;
  }

  private final String callerClassName;
  private final String callerMethodName;

  public ObfuscatedStr(String obfuscatedStr, int param, String callerClassName, String callerMethodName) {
    this.callerClassName = callerClassName;
    this.callerMethodName = callerMethodName;
    obfuscatedSourceStr = obfuscatedStr;
  }

  public String deobfuscate() {
    int preprocessedCharCode = 0;
    classHash = this.callerClassName.hashCode();
    methodHash = this.callerMethodName.hashCode();
    char[] obfuscatedChars2 = obfuscatedSourceStr.toCharArray();
    deobfuscatedChars = new char[obfuscatedChars2.length];
    obfuscatedChars = obfuscatedChars2;
    currCharIx = 0;
    while (true) {
      if (currCharIx >= obfuscatedChars.length) {
        var deobfuscatedStr = new String(deobfuscatedChars);
        /*var old = */
        deobfscatedCache.put(obfuscatedSourceStr, deobfuscatedStr);
        return deobfuscatedStr;
      }

      int switchCaseIx = currCharIx % 5 + 9;
      switch (switchCaseIx) {
        case 6: {
          String deobfuscatedStrFromCache = deobfscatedCache.get(obfuscatedSourceStr);
          if (deobfuscatedStrFromCache == null) {
            break;
          }

          return deobfuscatedStrFromCache;
        }
        case 9: {
          preprocessedCharCode = obfuscatedChars[currCharIx] ^ 4 + classHash;
          break;
        }
        case 10: {
          preprocessedCharCode = obfuscatedChars[currCharIx] ^ magicConst;
          break;
        }
        case 11: {
          preprocessedCharCode = obfuscatedChars[currCharIx] ^ classHash;
          break;
        }
        case 12: {
          preprocessedCharCode = obfuscatedChars[currCharIx] ^ methodHash;
          break;
        }
        case 13: {
          char currObfuscatedChar = obfuscatedChars[currCharIx];
          preprocessedCharCode = currObfuscatedChar ^ methodHash + classHash;
          break;
        }
        case 14: {
          preprocessedCharCode = obfuscatedChars[currCharIx] ^ currCharIx + methodHash;
          break;
        }
        default:
          throw new IndexOutOfBoundsException();
      }


      int magicIx = aiooi1iojionlknzjsdnfdas[currCharIx % CLASSES_COUNT];
      deobfuscatedChars[currCharIx] = (char) (preprocessedCharCode ^ magicIx);
      currCharIx = currCharIx + 1;
    }
  }
}

package me.deob.trickle;

public class IntUtil {
  public static int safeParse(String value) {
    try {
      return (int) Double.parseDouble(value);
    } catch (Exception ignore) {
      throw ignore;
    }
  }
}

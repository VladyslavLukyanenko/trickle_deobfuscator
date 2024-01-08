package me.deob;

import com.github.javaparser.ParseException;
import me.deob.trickle.TricklePostProcessor;
import org.jetbrains.java.decompiler.main.decompiler.TrickleDecompiler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
  public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    if (args.length != 2) {
      System.err.println("2 args required. 1 - classes dir, 2 - save results dir");
      System.out.println("Started with args:");
      for (var a : args) {
        System.out.println(a);
      }

      if (args.length == 0) {
        System.out.println("<No args>");
      }

      Thread.sleep(5000);
      System.exit(-1);
    }

    final Path classesDir = Paths.get(args[0]);
    final Path saveResultsDir = Paths.get(args[1]);

    final var strDeobfClsFile = classesDir.resolve( "c.class");
    final var classFileBytes = Files.readAllBytes(strDeobfClsFile);
    final var reader = new ClassReader(classFileBytes);
    final var node = new ClassNode();
    reader.accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
    final String targetMethodName = "0";
    final String targetMethodDescr = "(Ljava/lang/String;I)Ljava/lang/String;";
    final MethodNode method = node.methods.stream()
      .filter(m -> m.name.equals(targetMethodName) && m.desc.equals(targetMethodDescr))
      .limit(1)
      .toList()
      .get(0);

    int magicDeobfNum = -1;
    for (final var insn : method.instructions) {
      if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Integer i && Math.abs(i) > 1000) {
        magicDeobfNum = i;
        break;
      }
    }

    if (magicDeobfNum == -10) {
      throw new RuntimeException("Can't find magicNum");
    }

    final var classLoader = new URLClassLoader(new URL[] {
      classesDir.toUri().toURL()
    });

    classLoader.loadClass("0");
    final Class<?> cClass = classLoader.loadClass("c");
    Class.forName("c", true, classLoader);

    final var fField = cClass.getDeclaredField("aiooi1iojionlknzjsdnfdas");
    fField.setAccessible(true);
    final int[] numbersHolder = (int[]) fField.get(null);

    final String appClassesDirName = "io/trickle";
    final Path tempDir = Files.createTempDirectory("deobftrickle");
    final Path appClassesTmpDir = tempDir.resolve(appClassesDirName);
    final Path appDecompiledClassesDir = tempDir.resolve("decompiledSources");

    // copy app classes
    final Path appClassesDir = classesDir.resolve(appClassesDirName);
    final Path appPostprocessedClassesDir = saveResultsDir.resolve("src/main/java");

    copyFolder(appClassesDir, appClassesTmpDir);

    if (!Files.exists(appDecompiledClassesDir)) {
      appDecompiledClassesDir.toFile().mkdirs();
    }

    if (!Files.exists(saveResultsDir)) {
      saveResultsDir.toFile().mkdirs();
    }

    if (!Files.exists(appPostprocessedClassesDir)) {
      appPostprocessedClassesDir.toFile().mkdirs();
    }

    final var projectSkeleton = ClassLoader.getSystemClassLoader().getResourceAsStream("ProjectSkeleton.zip");
    unzip(projectSkeleton, saveResultsDir);
    copyFolder(classesDir, saveResultsDir.resolve("trickle"));
    deleteFolder(saveResultsDir.resolve("trickle/io/trickle"));

    TrickleDecompiler.decompile(appClassesTmpDir.toString(), appDecompiledClassesDir.toString(), magicDeobfNum, numbersHolder);
    TricklePostProcessor.process(appDecompiledClassesDir.toString(), saveResultsDir.resolve("src/main/java").toString());
    deleteFolder(tempDir);
  }

  private static void deleteFolder(Path dir) throws IOException {
    Files.walkFileTree(dir, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
  private static void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
    Files.walkFileTree(source, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
        Files.createDirectories(target.resolve(source.relativize(dir)));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
        throws IOException {
        Files.copy(file, target.resolve(source.relativize(file)), options);
        return FileVisitResult.CONTINUE;
      }
    });
  }
  private static void unzip(InputStream zipFile, Path outputPath) {
    try (ZipInputStream zis = new ZipInputStream(zipFile)) {

      ZipEntry entry = zis.getNextEntry();

      while (entry != null) {
        Path newFilePath = outputPath.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(newFilePath);
        } else {
          if (!Files.exists(newFilePath.getParent())) {
            Files.createDirectories(newFilePath.getParent());
          }
          try (OutputStream bos = Files.newOutputStream(outputPath.resolve(newFilePath))) {
            byte[] buffer = new byte[Math.toIntExact(entry.getSize())];

            int location;

            while ((location = zis.read(buffer)) != -1) {
              bos.write(buffer, 0, location);
            }
          }
        }
        entry = zis.getNextEntry();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
      //handle your exception
    }
  }
}

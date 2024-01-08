package me.deob.trickle;

import com.github.javaparser.ParseException;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TricklePostProcessor {
  private static final Predicate<String> validFilePredicate = Pattern.compile("\\\\io\\\\trickle\\\\.+\\.java").asPredicate();

  public static void process(String sourcesPath, String outputPath) throws IOException, ParseException {
    // Set up a minimal type solver that only looks at the classes used to run this sample.
    CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    combinedTypeSolver.add(new ReflectionTypeSolver());
    combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourcesPath)));

    // Configure JavaParser to use type resolution
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

    Class<class_0> clazz0 = class_0.class;
    var cls0Fields = new HashMap<String, Integer>();

    for (var f : clazz0.getDeclaredFields()) {
      f.setAccessible(true);
      try {
        var val = (Integer) f.get(null);
        cls0Fields.put(f.getName(), val);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    var sourceFiles = new ArrayList<File>();
    Files.walkFileTree(Paths.get(sourcesPath), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final String filePath = file.toAbsolutePath().toString();
        if (validFilePredicate.test(filePath)
            && !filePath.contains("ShopifyBackend.class")
            && !filePath.contains("ShopifySafe.class")
            && !filePath.contains("Aes.class")
            && !filePath.contains("FFX.class")
            && !filePath.contains("SDW.class")
            && !filePath.contains("CMAC.class")) {
          final File classFile = file.toFile();
          sourceFiles.add(classFile);
        }

        return super.visitFile(file, attrs);
      }
    });

    var isInnerClassPred = Pattern.compile("public (interface|enum|class) .+\\$.+", Pattern.MULTILINE).asPredicate();
    var problematicFiles = new HashMap<File, Exception>();
    var failures = Paths.get(outputPath).resolve("failures");
    for (var f : sourceFiles) {
      var dst = f.getAbsolutePath().replace(sourcesPath, outputPath);
      final var dstPath = Paths.get(dst);
      var clsName = f.getAbsolutePath()
          .replace(sourcesPath, "")
          .replace("\\", "/")
          .replace("/", ".");
      if (clsName.startsWith(".")) {
        clsName = clsName.substring(1);
      }

      var outputDir = dstPath.getParent();
      outputDir.toFile().mkdirs();

      var rawContent = Files.readString(f.toPath());
//      if (rawContent.contains("public enum ")) {
//        Files.writeString(outputPath, rawContent);
//        System.out.println("ENUM copied as is: " + clsName);
//        continue;
//      }

//      if (rawContent.contains("public interface ")) {
//        Files.writeString(outputPath, rawContent);
//        System.out.println("INTERFACE copied as is: " + clsName);
//        continue;
//      }
      try {
        CompilationUnit cu = StaticJavaParser.parse(f);
        processUnit(cls0Fields, cu);
        System.out.println("Processed: " + clsName);
        Files.writeString(dstPath, cu.toString());

      } catch (ParseProblemException pe) {
        if (rawContent.contains("public interface ") || isInnerClassPred.test(rawContent)) {
          Files.writeString(dstPath, rawContent);
          problematicFiles.put(f, pe);
          System.err.println("ERR: " + f);
        } else {
//          System.err.println(f);
//          throw pe;
          Files.writeString(dstPath, rawContent);
          problematicFiles.put(f, pe);
          System.err.println("ERR: " + f);
        }
      }  catch (Exception exc) {
//        var dst = Paths.get(f.getAbsolutePath().replace(SOURCES_PATH, failures.toString()));
//        dst.getParent().toFile().mkdirs();
//        Files.writeString(dst, Files.readString(f.toPath()));
        Files.writeString(dstPath, rawContent);
        problematicFiles.put(f, exc);
        System.err.println("ERR: " + f);
      }
    }

    // Parse some code
    System.out.println("DONE");
    if (problematicFiles.size() > 0) {
      System.out.println("ERRORS: " + problematicFiles.size());
      for (var e : problematicFiles.entrySet()) {
        var f = e.getKey();
        var clsName = f.getAbsolutePath()
            .replace(sourcesPath, "")
            .replace("\\", "/")
            .replace("/", ".");
        if (clsName.startsWith(".")) {
          clsName = clsName.substring(1);
        }

        final var exc = e.getValue();
        System.out.printf("%s: %s%n%n", clsName, exc.getMessage());
      }
    }
  }

  private static void processUnit(HashMap<String, Integer> cls0Fields, CompilationUnit cu) {
    // preprocess file globally

    // replace bool casts
    cu.findAll(CastExpr.class).forEach(c -> {
      if (c.getTypeAsString().equals("boolean") && c.getExpression() instanceof EnclosedExpr e && e.getInner() instanceof IntegerLiteralExpr ie) {
        c.replace(new BooleanLiteralExpr(ie.asNumber().intValue() != 0));
      }
    });

    // replace unary with literal
    cu.findAll(UnaryExpr.class).forEach(unary -> {
      final Expression valueExpr = unary.getExpression();
      if (valueExpr instanceof IntegerLiteralExpr intLiter) {
        final UnaryExpr.Operator operator = unary.getOperator();
        final TokenRange tokenRange = unary.getTokenRange().get();
        final IntegerLiteralExpr result = Util.calculateIntUnaryExpr(intLiter, operator, tokenRange);
        unary.replace(result);
      }
    });

    // inline `class_0` fields usages
    cu.findAll(FieldAccessExpr.class).forEach(fae -> {
      final Expression scope = fae.getScope();
      if (scope.isNameExpr() && scope.asNameExpr().getNameAsString().equals("class_0")) {
        final String value = cls0Fields.get(fae.getName().asString()).toString();
        fae.replace(new IntegerLiteralExpr(fae.getTokenRange().get(), value));
      }
    });

    // unwrap `if`
    cu.findAll(IfStmt.class).forEach(ifs -> {
      if (ifs.getCondition() instanceof BinaryExpr binaryExpr && binaryExpr.getLeft().isIntegerLiteralExpr() && binaryExpr.getRight().isIntegerLiteralExpr()) {
        final int l = binaryExpr.getLeft().asIntegerLiteralExpr().asNumber().intValue();
        final int r = binaryExpr.getRight().asIntegerLiteralExpr().asNumber().intValue();
        var ok = switch (binaryExpr.getOperator()) {
          case EQUALS -> l == r;
          case NOT_EQUALS -> l != r;
          case LESS -> l < r;
          case GREATER -> l > r;
          case LESS_EQUALS -> l <= r;
          case GREATER_EQUALS -> l >= r;
          case OR -> throw new RuntimeException();
          case AND -> throw new RuntimeException();
          case BINARY_OR -> throw new RuntimeException();
          case BINARY_AND -> throw new RuntimeException();
          case XOR -> throw new RuntimeException();
          case LEFT_SHIFT -> throw new RuntimeException();
          case SIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case UNSIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case PLUS -> throw new RuntimeException();
          case MINUS -> throw new RuntimeException();
          case MULTIPLY -> throw new RuntimeException();
          case DIVIDE -> throw new RuntimeException();
          case REMAINDER -> throw new RuntimeException();
        };

        if (ok) {
          if (ifs.hasElseBranch()) {
            ifs.removeElseStmt();
          } else {
            ifs.remove();
            return;
          }
        } else {
          if (!ifs.hasElseBranch()) {
            ifs.remove();
            return;
          } else {
            var els = ifs.getElseStmt().get();
            ifs.setThenStmt(els);
            ifs.removeElseStmt();
          }
        }

        var then = ifs.getThenStmt();
        var parent = (BlockStmt) ifs.getParentNode().get();
        var ix = parent.getStatements().indexOf(ifs);
        if (then instanceof BlockStmt blockStmt) {
          NodeList<Statement> statements = blockStmt.getStatements();
          for (int i = 0; i < statements.size(); i++) {
            Statement s = statements.get(i);
            if (s instanceof BreakStmt || s instanceof ContinueStmt) {
              break;
            }

            parent.addStatement(i + ix, s);
          }
        } else {
          parent.addStatement(ix, then);
        }

        ifs.remove();
      }
    });

    // fold assigning
    cu.findAll(AssignExpr.class).forEach(assignExpr -> {
      if (!(assignExpr.getValue() instanceof ConditionalExpr cond)) {
        return;
      }

      if (cond.getCondition() instanceof BinaryExpr binaryExpr && binaryExpr.getLeft().isIntegerLiteralExpr() && binaryExpr.getRight().isIntegerLiteralExpr()) {
        final int l = binaryExpr.getLeft().asIntegerLiteralExpr().asNumber().intValue();
        final int r = binaryExpr.getRight().asIntegerLiteralExpr().asNumber().intValue();
        var ok = switch (binaryExpr.getOperator()) {
          case EQUALS -> l == r;
          case NOT_EQUALS -> l != r;
          case LESS -> l < r;
          case GREATER -> l > r;
          case LESS_EQUALS -> l <= r;
          case GREATER_EQUALS -> l >= r;
          case OR -> throw new RuntimeException();
          case AND -> throw new RuntimeException();
          case BINARY_OR -> throw new RuntimeException();
          case BINARY_AND -> throw new RuntimeException();
          case XOR -> throw new RuntimeException();
          case LEFT_SHIFT -> throw new RuntimeException();
          case SIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case UNSIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case PLUS -> throw new RuntimeException();
          case MINUS -> throw new RuntimeException();
          case MULTIPLY -> throw new RuntimeException();
          case DIVIDE -> throw new RuntimeException();
          case REMAINDER -> throw new RuntimeException();
        };

        Expression expr = ok ? cond.getThenExpr() : cond.getElseExpr();
        assignExpr.setValue(expr);
      }
    });

    // fold var decl
    cu.findAll(VariableDeclarator.class).forEach(decl -> {
      if (!(decl.getInitializer().isPresent() && decl.getInitializer().get() instanceof ConditionalExpr cond)) {
        return;
      }

      if (cond.getCondition() instanceof BinaryExpr binaryExpr && binaryExpr.getLeft().isIntegerLiteralExpr() && binaryExpr.getRight().isIntegerLiteralExpr()) {
        final int l = binaryExpr.getLeft().asIntegerLiteralExpr().asNumber().intValue();
        final int r = binaryExpr.getRight().asIntegerLiteralExpr().asNumber().intValue();
        var ok = switch (binaryExpr.getOperator()) {
          case EQUALS -> l == r;
          case NOT_EQUALS -> l != r;
          case LESS -> l < r;
          case GREATER -> l > r;
          case LESS_EQUALS -> l <= r;
          case GREATER_EQUALS -> l >= r;
          case OR -> throw new RuntimeException();
          case AND -> throw new RuntimeException();
          case BINARY_OR -> throw new RuntimeException();
          case BINARY_AND -> throw new RuntimeException();
          case XOR -> throw new RuntimeException();
          case LEFT_SHIFT -> throw new RuntimeException();
          case SIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case UNSIGNED_RIGHT_SHIFT -> throw new RuntimeException();
          case PLUS -> throw new RuntimeException();
          case MINUS -> throw new RuntimeException();
          case MULTIPLY -> throw new RuntimeException();
          case DIVIDE -> throw new RuntimeException();
          case REMAINDER -> throw new RuntimeException();
        };

        Expression expr = ok ? cond.getThenExpr() : cond.getElseExpr();
        decl.setInitializer(expr);
      }
    });

    cu.findAll(SwitchEntry.class).forEach(switchEntry -> {
      if (switchEntry.getStatements().size() >= 1 && !(switchEntry.getStatements().get(0) instanceof BlockStmt)) {
        var blockExpr = new BlockStmt(switchEntry.getTokenRange().get(), switchEntry.getStatements());
        blockExpr.setParentNode(switchEntry);
//        switchEntry.setStatements(new NodeList<>(blockExpr));

      }
    });

    // todo: create blocks hierarchy with var defs + mutations and propagate to child blocks as loopback for vars
    // process blocks
//    if (1 != 1)
//for (var o = 0; o < 30; o++)
    cu.findAll(MethodDeclaration.class).forEach(md -> {
      var registry = new HashMap<String, BlockScopeImpl>();
      try {
        processSwitches(md, registry);
      } catch (Exception exc) {
        System.err.println("Can't process: " + md.getNameAsString());
      }
    });

    cu.findAll(ConstructorDeclaration.class).forEach(md -> {
      var registry = new HashMap<String, BlockScopeImpl>();
      try {
        processSwitches(md, registry);
      } catch (Exception exc) {
        System.err.println("Can't process: " + md.getNameAsString());
      }
    });

    cu.findAll(InitializerDeclaration.class).forEach(md -> {
      var registry = new HashMap<String, BlockScopeImpl>();
      try {
        processSwitches(md, registry);
      } catch (Exception exc) {
        System.err.println("Can't process: <" + (md.isStatic() ? "cl" : "") + "init>");
      }
    });

    cu.findAll(TryStmt.class).forEach(tryStmt -> {
      if (tryStmt.getTryBlock().getStatements().isEmpty()) {
        tryStmt.remove();
      }
    });
    cu.findAll(LabeledStmt.class).forEach(labeledStmt -> {
      if (!(labeledStmt.getStatement() instanceof TryStmt t)) {
        return;
      }

      if (t.getTryBlock().getStatements().isEmpty()) {
        labeledStmt.remove();
      }
    });

    final var whileStmts = cu.findAll(WhileStmt.class, Node.TreeTraversal.POSTORDER);
    for (int j = 0; j < whileStmts.size(); j++) {
      WhileStmt whileStmt = whileStmts.get(j);
      if (
          whileStmt.getCondition().isBooleanLiteralExpr()
              && whileStmt.getCondition().asBooleanLiteralExpr().getValue()
              && whileStmt.getBody() instanceof BlockStmt whileBody
      ) {
        if (
            whileBody.findAll(ContinueStmt.class).isEmpty()
                && whileBody.getStatement(whileBody.getStatements().size() - 1) instanceof ReturnStmt
                && whileStmt.getParentNode().get() instanceof BlockStmt parent
        ) {
          var ix = parent.getStatements().indexOf(whileStmt);
          NodeList<Statement> statements = whileBody.getStatements();
          for (int i = 0; i < statements.size(); i++) {
            Statement s = statements.get(i);
            parent.addStatement(ix + i, s);
          }

          whileStmt.remove();
        }
      }
    }
  }

  private static void processSwitches(Node cu, final Map<String, BlockScopeImpl> scopesRegistry) {
    cu.findAll(BlockStmt.class).forEach(bs -> {
      boolean wasParentRemoved = true;
      Node curr = bs;
      while (curr != null) {
        if (curr instanceof ClassOrInterfaceDeclaration || curr instanceof EnumDeclaration) {
          wasParentRemoved = false;
          break;
        }

        if (curr.getParentNode().isEmpty()) {
          break;
        }

        curr = curr.getParentNode().get();
      }

      if (wasParentRemoved) {
        return;
      }
//      var context = new VarsContext(declaredVars, varMutations);
      boolean eliminated;
      do {
        Scope scope = new BlockScopeImpl(bs, scopesRegistry);
        eliminated = false;
//        try {
        scope.populateScopeMutations();
//        } catch (RuntimeException x) {
//          continue;
//        }

        /*bs.findAll(SwitchStmt.class).forEach(sw -> {

        });*/

        NodeList<Statement> statements = bs.getStatements();
        // switch processing
        for (int stIx = 0; stIx < statements.size(); stIx++) {
          Statement s = statements.get(stIx);
          if (s instanceof LabeledStmt ls && ls.getStatement() instanceof SwitchStmt ss) {
            s.replace(ss);
            s = ss;
          }

          if (s instanceof SwitchStmt swtch) {
            if (swtch.getSelector() instanceof BinaryExpr be) {
              final var clone = be.clone();
              try {
                final var folded = Util.foldBinaryExprToLiteral(scope, swtch.getRange().get().begin, be);
                swtch.setSelector(folded);
              } catch (RuntimeException | MutationHasNoLiteralValueException exc) {
                swtch.setSelector(clone);
                continue;
              }
            }

            final Expression selector = swtch.getSelector();
            Object usedVarValue;
            if (selector instanceof LiteralStringValueExpr sve) {
              usedVarValue = sve.getValue();
            } else {
              String caseEntry = null;
              if (selector instanceof MethodCallExpr mcall) {
                if (mcall.getScope().isPresent()
                    && mcall.getScope().get() instanceof NameExpr ce
                    && ce.getNameAsString().equals("Integer")
                    && mcall.getArguments().size() == 1
                    && mcall.getArguments().get(0) instanceof NameExpr ne
                    && scope.containsVar(ne.getNameAsString())
                ) {
                  System.out.println("Integer.parse " + ne.getNameAsString());
                  caseEntry = ne.getNameAsString();
                }
              } else if (selector instanceof NameExpr nexpr) {
                caseEntry = nexpr.getNameAsString();
              }

              if (caseEntry == null) {
                continue;
              }

              VarMutation usedInAssignVar = scope.getLastMutation(caseEntry, swtch.getRange().get().begin);
              if (usedInAssignVar == null || !usedInAssignVar.isSimpleInitialized()) {
                continue;
//                throw new IllegalStateException("No mutation found: " + caseEntry);
              }

              usedVarValue = usedInAssignVar.value();
            }

            final SwitchEntry replacementEntry;
            try {
              replacementEntry = swtch.getEntries()
                  .stream()
                  .filter(e -> e.getLabels()
                      .stream()
                      .anyMatch(l ->
                          l instanceof LiteralExpr le
                              && Objects.equals(((LiteralStringValueExpr) le).getValue(), usedVarValue)
                      )
                  )
                  .findFirst()
                  .orElseGet(() -> swtch.getEntries()
                      .stream()
                      .filter(e -> e.getLabels().size() == 0)
                      .findFirst()
                      .get()
                  );
            } catch (Exception exc) {
              continue;
            }
            for (var insertSmIx = 0; insertSmIx < replacementEntry.getStatements().size(); insertSmIx++) {
              final Statement targetToInsert = replacementEntry.getStatement(insertSmIx);
              if (targetToInsert instanceof BreakStmt b && b.getLabel().isEmpty()) {
                break;
              }

              bs.addStatement(stIx + insertSmIx, targetToInsert);
            }

            bs.remove(swtch);
//            stIx--;
            eliminated = true;
            break;
          }
        }
      } while (eliminated);
/*
      NodeList<Statement> statements = bs.getStatements();
      for (int stIx = 0; stIx < statements.size(); stIx++) {
        processSwitches(statements.get(stIx));
      }*/
    });
  }
}
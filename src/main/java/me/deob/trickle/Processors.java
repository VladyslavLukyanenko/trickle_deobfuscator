package me.deob.trickle;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.*;

import java.util.*;

public class Processors {
  public static void processUnit(HashMap<String, Integer> cls0Fields, CompilationUnit cu) {
    preprocess(cls0Fields, cu);

    // todo: create blocks hierarchy with var defs + mutations and propagate to child blocks as loopback for vars
    // process blocks
//    if (1 != 1)
//for (var o = 0; o < 30; o++)
    cu.findAll(MethodDeclaration.class).forEach(md -> {
      var registry = new HashMap<String, BlockScopeImpl>();
      try {
        if (md.getParentNode().map(p -> p instanceof ObjectCreationExpr).orElse(false)) {
          // anon obj expr
          return;
        }
        processSwitches(md, registry);
        var methodsToProcess = md.findAll(MethodDeclaration.class);
        for (var method : methodsToProcess) {
          eliminateUnusedVars(method);
        }
      } catch (Exception exc) {
        System.err.println("Can't process: " + md.getNameAsString() + ", cause: " + exc.getMessage());
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

  private static void preprocess(HashMap<String, Integer> cls0Fields, CompilationUnit cu) {
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
  }

  public static void processSwitches(Node cu, final Map<String, BlockScopeImpl> scopesRegistry) {
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
//                  System.out.println("Integer.parse " + ne.getNameAsString());
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

  public static void eliminateUnusedVars(MethodDeclaration md) {
/*    if (1 == 1)
    return;*/

//    while (true) {

    var unusedVars = new ArrayList<String>();
    var usedVars = new HashSet<String>();

    md.findAll(VariableDeclarator.class).forEach(decl -> {
      if (!isContainedInMethod(md, decl)) {
        return;
      }

      unusedVars.add(decl.getNameAsString());
    });
    var initialCount = unusedVars.size();

    final var nodes = md.findAll(Node.class);
    for (int nodeIx = 0; nodeIx < nodes.size(); nodeIx++) {
      Node node = nodes.get(nodeIx);
      if (!isContainedInMethod(md, node)) {
        nodeIx++;
        continue;
      }

      for (int varIx = 0; varIx < unusedVars.size(); ) {
        String varName = unusedVars.get(varIx);
        if (isVarUsedBy(node, varName)) {
          usedVars.add(varName);
          unusedVars.remove(varName);
          nodeIx--;
          break;
        } else {
          varIx++;
        }
      }
    }

    md.findAll(AssignExpr.class).forEach(a -> {
      if (a.getTarget() instanceof NameExpr n && unusedVars.contains(n.getNameAsString())) {
        if (!isContainedInMethod(md, a)) {
          return;
        }

        var stmt = getParentOfType(a, ExpressionStmt.class);
        stmt.remove();
//        a.getParentNode().get().removeComment().remove(a);
      }
    });
    md.findAll(VariableDeclarator.class).forEach(a -> {
      if (!isContainedInMethod(md, a)) {
        return;
      }

      if (unusedVars.contains(a.getNameAsString())) {
        var p = a.getParentNode().orElse(null);
        a.remove();
        if (p instanceof VariableDeclarationExpr e && e.getVariables().isEmpty() && e.getParentNode().get() instanceof ExpressionStmt es) {
          es.removeComment().remove();
        }
      }
    });
//
//      if (initialCount != unusedVars.size()) {
//        continue;
//      }

    printDebug("UNUSED: " + String.join(", ", unusedVars));
    printDebug("used: " + String.join(", ", usedVars));
//      break;
//    }

  }

  private static void printDebug(String x) {
//    System.out.println(x);
  }

  private static boolean isContainedInMethod(MethodDeclaration md, Node node) {
    Node parent = node.getParentNode().orElse(null);
    if (parent == null) {
      return false;
    }

    boolean declaredInCurrentMethod = false;
    while (parent != null) {
      if (parent instanceof MethodDeclaration currMd) {
        declaredInCurrentMethod = currMd == md;
        break;
      }

      parent = parent.getParentNode().orElse(null);
    }
    return declaredInCurrentMethod;
  }

  private static boolean isVarUsedBy(Node node, String varName) {
    // - field set == assigned on right side
    // - assigned on right side
    if (node instanceof VariableDeclarator declarator) {
      if (declarator.getInitializer().map(initializer -> isVarUsedByName(initializer, varName)).orElse(false)) {
        printDebug("VariableDeclarator " + varName);
        return true;
      }
    }
    if (node instanceof AssignExpr assignExpr) {
      if (!(assignExpr.getTarget() instanceof NameExpr an && an.getNameAsString().equals(varName)) && isVarUsedByName(assignExpr.getValue(), varName)) {
        printDebug("AssignExpr " + varName);
        return true;
      }
    }

    // - new obj: ObjectCreationExpr, arguments: NodeList[] [ix]: NameExpr
    // - method call: MethodCallExpr, scope: NameExpr
    // - pass as arg: MethodCallExpr, arguments: NodeList[] [ix]: NameExpr
    if (node instanceof MethodCallExpr methodCallExpr) {
      if (methodCallExpr.getScope().map(scope -> isNotObjectCreation(scope) && isVarUsedByName(scope, varName)).orElse(false)) {
        printDebug("MethodCallExpr " + varName);
        return true;
      } else if (methodCallExpr.getArguments().stream().anyMatch(a -> isVarUsedByName(a, varName))) {
        printDebug("MethodCallExpr " + varName);
        return true;
      }
    }
    if (node instanceof ObjectCreationExpr objectCreationExpr) {
      if (objectCreationExpr.getScope().map(scope -> scope.isNameExpr() && isVarUsedByName(scope, varName)).orElse(false)) {
        printDebug("ObjectCreationExpr " + varName);
        return true;
      } else if (objectCreationExpr.getArguments().stream().anyMatch(a -> !a.isObjectCreationExpr() && isVarUsedByName(a, varName))) {
        printDebug("ObjectCreationExpr " + varName);
        return true;
      }
    }

    // - binary operand: if contains NameExpr
    // - logical operand: contains NameExpr
    if (node instanceof BinaryExpr b && isVarUsedByName(b, varName)) {
      var assign = getParentOfType(b, AssignExpr.class);
      if (assign == null || !(assign.getTarget() instanceof NameExpr an && an.getNameAsString().equals(varName))) {
        printDebug("BinaryExpr " + varName);
        return true;
      }
    }
    // - loop argument: WhileStmt/NodeWithCondition, condition: NameExpr
    // - if: IfStmt, condition: NameExpr
    if (node instanceof NodeWithCondition c && isVarUsedByName(c.getCondition(), varName)) {
      printDebug("NodeWithCondition " + varName);
      return true;
    }
    if (node instanceof ForStmt c && (
        c.getCompare().map(co -> isVarUsedByName(co, varName)).orElse(false)
            /*|| c.getInitialization().stream().anyMatch(in -> varUsed(in, varName))
            || c.getUpdate().stream().anyMatch(u -> varUsed(u, varName))*/)
    ) {
      printDebug("ForStmt " + varName);
      return true;
    }

    // - switch argument: SwitchStmt, selector: NameExpr
    if (node instanceof SwitchStmt sw && isVarUsedByName(sw.getSelector(), varName)) {
      printDebug("SwitchStmt " + varName);
      return true;
    }


    // - array initializer: (ArrayInitiailizerExpr, values: NodeList { NameExpr }
    if (node instanceof ArrayInitializerExpr init && isVarUsedByName(init, varName)) {
      printDebug("ArrayInitializerExpr " + varName);
      return true;
    }

    // - array indexer param: ArrayAccessExpr, index: NameExpr
    if (node instanceof ArrayAccessExpr arrAcc && (isVarUsedByName(arrAcc.getIndex(), varName) || isVarUsedByName(arrAcc.getName(), varName))) {
      printDebug("ArrayAccessExpr " + varName);
      return true;
    }

    // - return stmt value: ReturnStmt, expression contains NameExpr
    if (node instanceof ReturnStmt r && r.getExpression().map(e -> isVarUsedByName(e, varName)).orElse(false)) {
      printDebug("ReturnStmt " + varName);
      return true;
    }

    return false;
  }

  private static boolean isNotObjectCreation(Expression scope) {
    return scope.findAll(ObjectCreationExpr.class).size() == 0;
  }

  private static boolean isVarUsedByName(Node node, String varName) {
    if (node instanceof NameExpr ne) {
      return ne.getNameAsString().equals(varName);
    }

    return node.findAll(NameExpr.class).stream().anyMatch(n -> n.getNameAsString().equals(varName));
  }

  private static <T extends Node> T getParentOfType(Node node, Class<T> expectedParent) {
    Node parent = node.getParentNode().orElse(null);
    if (parent == null) {
      return null;
    }

    while (parent != null) {
      if (parent.getClass().equals(expectedParent)) {
        return (T) parent;
      }

      parent = parent.getParentNode().orElse(null);
    }

    return null;
  }
}


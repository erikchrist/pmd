/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.symboltable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTCatchStatement;
import net.sourceforge.pmd.lang.java.ast.ASTEqualityExpression;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.Scope;
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration;

import org.junit.Test;
public class AcceptanceTest extends STBBaseTst {

    @Test
    public void testClashingSymbols() {
        parseCode(TEST1);
    }

    @Test
    public void testInitializer() {
        parseCode(TEST_INITIALIZERS);
        ASTInitializer a = acu.findDescendantsOfType(ASTInitializer.class).get(0);
        assertFalse(a.isStatic());
        a = acu.findDescendantsOfType(ASTInitializer.class).get(1);
        assertTrue(a.isStatic());
    }

    @Test
    public void testCatchBlocks() {
        parseCode(TEST_CATCH_BLOCKS);
        ASTCatchStatement c = acu.findDescendantsOfType(ASTCatchStatement.class).get(0);
        ASTBlock a = c.findDescendantsOfType(ASTBlock.class).get(0);
        Scope s = a.getScope();
        Map<VariableNameDeclaration, List<NameOccurrence>> vars = s.getParent().getVariableDeclarations();
        assertEquals(1, vars.size());
        VariableNameDeclaration v = vars.keySet().iterator().next();
        assertEquals("e", v.getImage());
        assertEquals(1, (vars.get(v)).size());
    }

    @Test
    public void testEq() {
        parseCode(TEST_EQ);
        ASTEqualityExpression e = acu.findDescendantsOfType(ASTEqualityExpression.class).get(0);
        ASTMethodDeclaration method = e.getFirstParentOfType(ASTMethodDeclaration.class);
        Scope s = method.getScope();
        Map<VariableNameDeclaration, List<NameOccurrence>> m = s.getVariableDeclarations();
        assertEquals(2, m.size());
        for (Map.Entry<VariableNameDeclaration, List<NameOccurrence>> entry : m.entrySet()) {
            VariableNameDeclaration vnd = entry.getKey();
            List<NameOccurrence> usages = entry.getValue();

            if (vnd.getImage().equals("a") || vnd.getImage().equals("b")) {
                assertEquals(1, usages.size());
                assertEquals(3, usages.get(0).getLocation().getBeginLine());
            } else {
                fail("Unkown variable " + vnd);
            }
        }
    }

    @Test
    public void testFieldFinder() {
        parseCode(TEST_FIELD);
//        System.out.println(TEST_FIELD);

        ASTVariableDeclaratorId declaration = acu.findDescendantsOfType(ASTVariableDeclaratorId.class).get(1);
        assertEquals(3, declaration.getBeginLine());
        assertEquals("bbbbbbbbbb", declaration.getImage());
        assertEquals(1, declaration.getUsages().size());
        NameOccurrence no = declaration.getUsages().get(0);
        JavaNode location = no.getLocation();
        assertEquals(6, location.getBeginLine());
//        System.out.println("variable " + declaration.getImage() + " is used here: " + location.getImage());
    }

    @Test
    public void testDemo() {
        parseCode(TEST_DEMO);
//        System.out.println(TEST_DEMO);
        ASTMethodDeclaration node = acu.findDescendantsOfType(ASTMethodDeclaration.class).get(0);
        Scope s = node.getScope();
        Map<VariableNameDeclaration, List<NameOccurrence>> m = s.getVariableDeclarations();
        for (Iterator<VariableNameDeclaration> i = m.keySet().iterator(); i.hasNext();) {
            VariableNameDeclaration d = i.next();
            assertEquals("buz", d.getImage());
            assertEquals("ArrayList", d.getTypeImage());
            List<NameOccurrence> u = m.get(d);
            assertEquals(1, u.size());
            NameOccurrence o = u.get(0);
            int beginLine = o.getLocation().getBeginLine();
            assertEquals(3, beginLine);

//            System.out.println("Variable: " + d.getImage());
//            System.out.println("Type: " + d.getTypeImage());
//            System.out.println("Usages: " + u.size());
//            System.out.println("Used in line " + beginLine);
        }
    }

    @Test
    public void testEnum() {
	parseCode(NameOccurrencesTest.TEST_ENUM);

	ASTVariableDeclaratorId vdi = acu.findDescendantsOfType(ASTVariableDeclaratorId.class).get(0);
	List<NameOccurrence> usages = vdi.getUsages();
	assertEquals(2, usages.size());
	assertEquals(5, usages.get(0).getLocation().getBeginLine());
	assertEquals(9, usages.get(1).getLocation().getBeginLine());
    }

    private static final String TEST_DEMO =
            "public class Foo  {" + PMD.EOL +
            " void bar(ArrayList buz) { " + PMD.EOL +
            "  buz.add(\"foo\");" + PMD.EOL +
            " } " + PMD.EOL +
            "}" + PMD.EOL;

    private static final String TEST_EQ =
            "public class Foo  {" + PMD.EOL +
            " boolean foo(String a, String b) { " + PMD.EOL +
            "  return a == b; " + PMD.EOL +
            " } " + PMD.EOL +
            "}" + PMD.EOL;

    private static final String TEST1 =
            "import java.io.*;" + PMD.EOL +
            "public class Foo  {" + PMD.EOL +
            " void buz( ) {" + PMD.EOL +
            "  Object o = new Serializable() { int x; };" + PMD.EOL +
            "  Object o1 = new Serializable() { int x; };" + PMD.EOL +
            " }" + PMD.EOL +
            "}" + PMD.EOL;

    private static final String TEST_INITIALIZERS =
            "public class Foo  {" + PMD.EOL +
            " {} " + PMD.EOL +
            " static {} " + PMD.EOL +
            "}" + PMD.EOL;

    private static final String TEST_CATCH_BLOCKS =
            "public class Foo  {" + PMD.EOL +
            " void foo() { " + PMD.EOL +
            "  try { " + PMD.EOL +
            "  } catch (Exception e) { " + PMD.EOL +
            "   e.printStackTrace(); " + PMD.EOL +
            "  } " + PMD.EOL +
            " } " + PMD.EOL +
            "}" + PMD.EOL;

    private static final String TEST_FIELD =
            "public class MyClass {" + PMD.EOL +
            " private int aaaaaaaaaa; " + PMD.EOL +
            " boolean bbbbbbbbbb = MyClass.ASCENDING; " + PMD.EOL +
            " private int zzzzzzzzzz;" + PMD.EOL +
            " private void doIt() {" + PMD.EOL +
            "  if (bbbbbbbbbb) {" + PMD.EOL +
            "  }" + PMD.EOL +
            " }" + PMD.EOL +
            "}" + PMD.EOL;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(AcceptanceTest.class);
    }
}

package nyu.segfault;

/**
 * Java SE library import statements
 */
import java.io.*;
import java.util.*;

/**
 * xtc import statements
 */

import xtc.lang.JavaFiveParser;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.util.SymbolTable;
import xtc.util.Tool;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;


/**
 * SegHead Visitor  handles classes without inheritance and virtual methods
 */
public class SegHead extends Visitor{

	HashSet<String> publicHPP; /**@var fields that fall under the public access modifier */
	HashSet<String> privateHPP; /**@var fields that fall under the private access modifer */

	HashSet<String> publicHPPMethods; /**@var methods that fall under public access modifier */
	HashSet<String> privateHPPMethods; /**@var methods that fall under private access modifier */

	/**
	 * default constructor for SegHead
	 */
	public SegHead(){
        this.privateHPP = new HashSet<String>();
        this.publicHPP = new HashSet<String>();
        this.privateHPPMethods = new HashSet<String>();
        this.publicHPPMethods = new HashSet<String>();
	}
	public void visitCompilationUnit(GNode n){
		SegHelper.writeMacros();
		SegHelper.endMacroScopes();
        SegHelper.hpp_pln("");
        SegHelper.hpp_pln("using namespace java::lang;\n");
        for (String className : SegHelper.allDeclaredClassNames) {
            SegHelper.hpp_pln("struct __" + className + ";");
            SegHelper.hpp_pln("struct __" + className + "_VT;");
            SegHelper.hpp_pln("");
        }

        for (String className : SegHelper.allDeclaredClassNames) {
            SegHelper.hpp_pln("typedef __rt::Ptr<__" + className + "> " + className + ";");
        }
        SegHelper.hpp_pln("");

        visit(n);
    }


	public void visitClassDeclaration(GNode n) {
        String className = n.getString(1);
        SegHelper.currClass = className;
        SegHelper.classToAllAvailableMethodDeclarations.put(className, new ArrayList<String>());

        // Print the virtual table pointer data field.
		SegHelper.hpp_pln(SegHelper.getClassDeclaration(n) + " {");
        SegHelper.hpp_pln("\t// Virtual table pointer.");
        SegHelper.hpp_pln("\t__" + SegHelper.getClassName(n) + "_VT* __vptr;");

        // Print the constructor.
        SegHelper.hpp_pln("\n\t// The constructor.");
        SegHelper.hpp_pln("\t__" + className + "();");

        // Print the data fields.
        SegHelper.hpp_pln("\n\t// The data fields.");
        ArrayList<String> dataFields = SegHelper.getGlobalVariables(n);
        for (String field : dataFields) {
            SegHelper.hpp_pln("\t" + field + ";");
        }
        SegHelper.hpp_pln("");

        /* The following two blocks are crucial for correct listing of methods in the header file. */
        // Get the Object superclass method declarations.
        for (String methodDeclaration : SegHelper.getObjectMethodDeclarations()) {
            String tailoredDeclaration = SegHelper.getDeclarationWithNewThisParameter(methodDeclaration, className);
            SegHelper.classToAllAvailableMethodDeclarations.get(className).add(tailoredDeclaration);
        }

        // Get any other superclass method declarations, in order of declarations from the farthest class
        // (direct descendants of Object) to the nearest class.
        ArrayList<String> superclasses = SegHelper.getListOfSuperclasses(className);
        int numberOfSuperClasses = superclasses.size();
        for (int c = 1; c < numberOfSuperClasses; c++) {  // Start at 1 to ignore Object methods, which are already accounted for.
            ArrayList<String> superclassDeclarations = SegHelper.classNameToMethodDeclarations.get(superclasses.get(c));
            for (String declaration : superclassDeclarations) {  // Print the declarations of this super class (with the modified "this" parameter).
                String tailoredDeclaration = SegHelper.getDeclarationWithNewThisParameter(declaration, className);
                SegHelper.classToAllAvailableMethodDeclarations.get(className).add(tailoredDeclaration);
            }
        }

        // Print the init declaration.
        SegHelper.hpp_pln("");
        String constructorParameterList;
        System.out.println(n +"\n\n\n");
        SegHelper.hpp_pln("static " + className + " init(" + className + ");");
        SegHelper.hpp_pln("");

        // Print this class's method declarations.
        SegHelper.hpp_pln("\t// This class's method declarations.");
        for (String methodDeclaration : SegHelper.classNameToMethodDeclarations.get(className)) {
            SegHelper.hpp_pln("\t" + methodDeclaration + ";");
            SegHelper.classToAllAvailableMethodDeclarations.get(className).add(methodDeclaration);
        }

        // Print the function returning this class's object.
        SegHelper.hpp_pln("\n\t// The function returning the class object representing " + SegHelper.getClassName(n) + ".");
        SegHelper.hpp_pln("\tstatic Class __class();\n");

        // If this class does not explicitly extend a class, use Object's declarations. Else, use the declarations of
        // the first superclass that uses the same

        // Print the virtual table data field.
        SegHelper.hpp_pln("\t// The vtable for " + SegHelper.getClassName(n) + ".");
        SegHelper.hpp_pln("\tstatic __" + SegHelper.getClassName(n) + "_VT __vtable;");

		SegHelper.hpp_pln("};\n\n");

		/**generate vtable for that respective class*/
		SegHelper.generateVtableForClass(className);
	}


	public void visitMethodDeclaration(GNode n){
        if (n.getNode(2) == null) return;  // This is a constructor

//		String method_decl=SegHelper.getMethodDeclaration(n,SegHelper.getCurrClass());
//		if(method_decl != null){
//			SegHelper.hpp_pln("\t"+method_decl);
//		}
//        String pointer = SegHelper.getMethodPointerFromDeclaration(n);
//        System.out.println(pointer);
	}


	public void visit(GNode n) {
		for (Object o : n) if (o instanceof Node) dispatch((Node) o);
	}
}

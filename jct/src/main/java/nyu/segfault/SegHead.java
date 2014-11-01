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
			
	}
	public void visitCompilationUnit(GNode n){
		SegHelper.writeMacros();		
		

	}
	public void visitClassDeclaration(GNode n){
		SegHelper.hpp_pln(SegHelper.getClassDeclaration(n));
		
		this.privateHPP = new HashSet<String>();
		this.publicHPP = new HashSet<String>();
		this.privateHPPmethods = new HashSet<String>();
		this.publicHPPmethods = new HashSet<String>();
		
	}
	public void visit(GNode n) {
		for (Object o : n) if (o instanceof Node) dispatch((Node) o);
	}
}

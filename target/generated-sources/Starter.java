import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

public class Starter {

	static TreeViewer viewr;
	static ArrayList<Block> representation;
	static HashMap<String, ClassDeclaration> classes;
	static int count = 0;

	private static boolean syntaxErrors = false;

	public static void main(String[] args) throws IOException {

		String inputFile = null;
		String outputFile=null;
		if (args.length > 1) {
			inputFile = args[0];
			outputFile = args[1];
		} else {
			System.out.println("Wrong use of program. Should be:");
			System.out.println("Starter <inputfile> <outputfile>");
			System.exit(-1);
		}
		InputStream is = System.in;
		if (inputFile != null)
			is = new FileInputStream(inputFile);
		else {
			System.out.println("error in file");
			return;
		}
		ANTLRInputStream input = new ANTLRInputStream(is);
		JavaLexer lexer = new JavaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		lexer.removeErrorListeners();
		lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
		parser.removeErrorListeners();
		parser.addErrorListener(DescriptiveErrorListener.INSTANCE);
		ParserRuleContext tree = parser.compilationUnit(); // parse
		if (syntaxErrors) {
			System.out
					.println("There are syntax errors, but continuing with semantic analises");
		}

		viewr = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
		viewr.setScale(1.5);// scale a little

		PanAndZoom pan = new PanAndZoom(viewr);

		// System.out.println(tree.toStringTree(parser)); // print LISP-style
		// tree
		ParseTreeWalker walker = new ParseTreeWalker(); // create standard
														// walker
		representation = new ArrayList<Block>();
		MyListener extractor = new MyListener();

		walker.walk(extractor, tree); // initiate walk of tree with listener
		try {
			for (Block block : representation) {
				// block.print();
				block.processToJava();
			}
		} catch (NullPointerException e) {
			System.out
					.println("Error in processing java code because of previous error");
		}

		WriteToFile(inputFile, outputFile, representation);
	}

	public static void SyntaxError() {
		syntaxErrors = true;
	}

	public static void WriteToFile(String srcfile, String destfile,
			ArrayList<Block> blocks) {
		Scanner sc;
		Scanner sc1 = null;
		PrintWriter pw = null;
		try {
			sc = new Scanner(new File(srcfile));
			pw = new PrintWriter(new File(destfile));
			sc.useDelimiter("@->JQ");
			String firstPart = sc.next();
			// System.out.print(firstPart);
			pw.print(firstPart);
			int i = 0;
			while (sc.hasNext()) {
				String codigomisto = sc.next();

				// codigo traduzido
				ArrayList<Declaration> dec = blocks.get(i).getDeclarations();
				for (Declaration decla : dec) {
					String declaration = decla.getJavaCode();
					if (declaration != null) {
						pw.print(decla.getJavaCode());
					}
				}
				sc1 = new Scanner(codigomisto);
				sc1.useDelimiter("@<-JQ");
				sc1.next();
				String next1 = sc1.next();
				// System.out.println(next1);
				pw.print(next1);
				i++;

			}
			sc.close();
			sc1.close();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}

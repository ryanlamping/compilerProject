package stackmachine.compiler.sprint1;
import slu.compiler.*;

public class TestProgram {

	public static void printTokens(String program) throws Exception {
		IToken tokenName;
		IScanner scanner = new Scanner(program);
		
		do {
			tokenName = scanner.getToken();
			
			System.out.println("<" + tokenName.toString() + ">");
			
		} while (!tokenName.getName().equals("null"));

		System.out.println("");
	}
	
	public static void main(String[] args) {
		boolean showTokens = false;
		
		String program = "void main { int a, b, c, d; float x, y, z; boolean halt; }";

		try {
			
			if (showTokens) {
				printTokens(program);
			}
			
			Parser parser = new Parser( new Scanner(program) );
		
			parser.compile();
		
			System.out.println("The symbol table \n\n" + parser.symbolTable());
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}
	}

}
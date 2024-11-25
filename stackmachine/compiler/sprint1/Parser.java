package stackmachine.compiler.sprint1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for declaration of variables
 *  
 *     program                  ->  void main { declarations }
 *
 *     declarations             ->  declaration declarations  |
 *                                  epsilon
 *                                     
 *     declaration              ->  type { identifiers.val = type.val } identifiers ;
 *     
 *     type                     ->  int     { type.val = "int"     } |
 *                                  float   { type.val = "float"   } |
 *                                  boolean { type.val = "boolean" }                                   
 *                  
 *     identifiers              ->  id 
 *                                  { addSymbol(id.lexeme, identifiers.val); more-identifiers.val = identifiers.val }
 *                                  more-identifiers
 *                                      
 *     more-identifiers         ->  , id 
 *                                  { addSymbol(id.lexeme, more-identifiers.val) }
 *                                  more-identifiers |
 *                                  epsilon
 *  
 */

public class Parser implements IParser {
    private IToken token;
    private IScanner scanner;
    // HashTable to hold symbols
    private Map<String, IDataType> symbols;
    
    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
    }
    
    @Override
    public String symbolTable() {
        String symbols = "";
        
        // Creates object with [key=value, key=value]
        Set<Map.Entry<String, IDataType>> s = this.symbols.entrySet();

        // Printing out the symbol table
        for (Map.Entry<String, IDataType> m : s) {
            symbols = symbols + "<'" + m.getKey() + "', " + m.getValue().toString() + "> \n";
        }
        
        return symbols;
    }
    
    @Override
    public void compile() throws Exception {
        program();
    }

    private void program() throws Exception {
        // Matches exact grammar definition of starting non-terminal symbol
        match("void");
        match("main");
        match("open-curly-bracket");
        
        declarations();
        
        match("closed-curly-bracket");
    }
    
    private void declarations() throws Exception {

       // if the current token is a primitive data type "int", "float" or "boolean", call functions declaration and declarations
       // otherwise do nothing, since the rule declarations produce epsilon
       
       if (this.token.getName().equals("int") || this.token.getName().equals("float") || 
       this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }

       // or epsilon --> no else
    }

    private void declaration() throws Exception {
        // how does identifiers on the outside work?
        identifiers(type());
        match("semicolon");    
    }

    private String type() throws Exception {
        String type = this.token.getName();
        
        if (type.equals("int")) {
            match("int");
        } else if (type.equals("float")) {
            match("float");
        } else if (type.equals("boolean")) {
            match("boolean");
        } else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": data type expected");
        }
        
        return type;
    }

    private void identifiers(String type) throws Exception {
		if (this.token.getName().equals("id")) {
            // Downcast to identifier
			Identifier id = (Identifier) this.token;

            // Check to see if the id is in the hashtable
			if (this.symbols.get(id.getLexeme()) == null)
				this.symbols.put(id.getLexeme(), new PrimitiveType(type));
			else {
				throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
			}
		
			match("id");
			
			moreIdentifiers(type);
		} else {
			throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");
		}
	}
	
	private void moreIdentifiers(String type) throws Exception {
        // Must start with a comma
		if (this.token.getName().equals("comma")) {
			match("comma");
			
            // Must be an id
			if (this.token.getName().equals("id")) {
				Identifier id = (Identifier) this.token;

				if (this.symbols.get(id.getLexeme()) == null) {
					this.symbols.put(id.getLexeme(), new PrimitiveType(type));
				} else {
					throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
				}

				match("id");

				moreIdentifiers(type);
			} else {
				throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");				
			}		
		}
	}
    
    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName))  {
            this.token = this.scanner.getToken();
        } else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " +
               (this.scanner.getLexeme(tokenName).equals("null") ? "token " + tokenName + " is not defined in 'lexicon.txt'" : tokenName + " expected"));
        }
    }
}

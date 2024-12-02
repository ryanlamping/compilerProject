package stackmachine.compiler.sprint2;

import java.util.HashMap;
import java.util.Map;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for assignments in declarations and arithmetic expressions (Sprint 2)
 *  
 *     program                  ->  void main { declarations statements } generateCode("halt")
 *
 *     declarations             ->  declaration declarations  |
 *                                  epsilon
 *                                     
 *     declaration              ->  type { identifiers.type = type.value } identifiers ;
 *     
 *     type                     ->  int     { type.value = "int"     } |
 *                                  float   { type.value = "float"   } |
 *                                  boolean { type.value = "boolean" }                                   
 *                  
 *     identifiers              ->  id 
 *                                  { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                                  optional-declaration
 *                                  more-identifiers
 *                                      
 *     more-identifiers         ->  , id 
 *                                  { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                                  optional-declaration
 *                                  more-identifiers |
 *                                  epsilon
 *                           
 *     optional-declaration     ->  = { generateCode("addressof " + id.lexeme) } arithmetic-expression { generateCode("store") } |
 *                                  epsilon
 *                             
 *     statements               ->  statement statements |
 *                                  epsilon
 *                              
 *     statement                ->  declaration |
 *                                  id assignment-expression ;
 *                                  
 *     assignment-expression    ->  = { generateCode("addressof " + id.lexeme) } arithmetic-expression { generateCode("store") }                        
 *
 *     arithmetic-expression    ->  arithmetic-term more-arithmetic-terms
 *     
 *     more-arithmetic-terms    ->  + arithmetic-term { generateCode("+") } more-arithmetic-terms |
 *                                  - arithmetic-term { generateCode("-") } more-arithmetic-terms |
 *                                  epsilon
 *               
 *     arithmetic-term          ->  arithmetic-factor more-arithmetic-factors
 *     
 *     more-arithmetic-factors  ->  * arithmetic-factor { generateCode("*") } more-arithmetic-factors |
 *                                  / arithmetic-factor { generateCode("/") } more-arithmetic-factors |
 *                                  % arithmetic-factor { generateCode("%") } more-arithmetic-factors |
 *                                  epsilon
 *                    
 *     arithmetic-factor        ->  (arithmetic-expression) |
 *                                   id  { generateCode("addressof " + id.lexeme); generateCode("load") } |
 *                                   int { generateCode("push " + int.value) }
 *  
 */

public class Parser implements IParser {
    private IToken token;
    private IScanner scanner;
    private IIntermediateCode code;
    private Map<String, IDataType> symbols;
    
    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new IntermediateCode();
    }

    @Override
    public String compile() throws Exception {
        program();

        return this.code.toString();
    }
    
    private void program() throws Exception {
        match("void");
        match("main");
        match("open-curly-bracket");
        
        declarations();
        statements();
        
        match("closed-curly-bracket");
        
        this.code.generate("halt");
    }
    
    private void declarations() throws Exception {
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
    }

    private void declaration() throws Exception {
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
			
			Identifier id = (Identifier) this.token;

			if (this.symbols.get(id.getLexeme()) == null) {
				this.symbols.put(id.getLexeme(), new PrimitiveType(type));
			} else {
				throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
			}
		 
			match("id");
			
			optionalDeclaration(type, id);
			
			moreIdentifiers(type);
			
		} else {
			
			throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");
			
		}
	}
    
	private void moreIdentifiers(String type) throws Exception {
		if (this.token.getName().equals("comma")) {
			
			match("comma");
			
			if (this.token.getName().equals("id")) {
				Identifier id = (Identifier) this.token;

				if (this.symbols.get(id.getLexeme()) == null) {
					this.symbols.put(id.getLexeme(), new PrimitiveType(type));
				} else {
					throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
				}
				
				match("id");
				
				optionalDeclaration(type, id);

				moreIdentifiers(type);
				
			} else {
				
				throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");
				
			}
		}
	}
	
    private void optionalDeclaration(String type, Identifier id) throws Exception {
		if (this.token.getName().equals("assignment")) {
			match("assignment");
			
			// the token 'assignment' allows to assign a value to a variable in the declaration

			this.code.generate("addressof " + id.getLexeme());
			
			arithmeticExpression();
			
			this.code.generate("store");
		}
    }    

    private void statements() throws Exception {
		String tokenName = this.token.getName();

		// check the tokens in FIRST(statement)
		
		if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id")) {			
			statement();
			statements();			
		}
    }
    
    private void statement() throws Exception {
		String tokenName = this.token.getName();
		
		// check the tokens in FIRST(statement)
		
		if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {
			
			declaration();
			
		} else if (tokenName.equals("id")) {

			assignmentExpression();
			match("semicolon");
			
		}
    }

    private void assignmentExpression() throws Exception {        
    	Identifier id = (Identifier) this.token;
        
        this.code.generate("addressof " + id.getLexeme());
        
        match("id");
        match("assignment");
        
        arithmeticExpression();
        
        this.code.generate("store");
    }

    private void arithmeticExpression() throws Exception {
        arithmeticTerm(); moreArithmeticTerms();
    }
        
    private void arithmeticTerm() throws Exception {
        arithmeticFactor(); moreArithmeticFactors();
    }
        
    private void moreArithmeticTerms() throws Exception {
        if (this.token.getName().equals("add")) {
            
            match("add");
            
            arithmeticTerm();
                        
            this.code.generate("+");
            
            moreArithmeticTerms();

        } else if (this.token.getName().equals("subtract")) {
            
            match("subtract");
            
            arithmeticTerm();

            this.code.generate("-");
            
            moreArithmeticTerms();
            
        } 
    }

    private void arithmeticFactor() throws Exception {        
        if (this.token.getName().equals("open-parenthesis")) {
            
            match("open-parenthesis");
            
            arithmeticExpression();
            
            match("closed-parenthesis");
        
        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;
            
            this.code.generate("addressof " + id.getLexeme());
            this.code.generate("load");

            match("id");

        } else if (this.token.getName().equals("int")) {
            
            IntegerNumber number = (IntegerNumber) this.token;
                            
            this.code.generate("push " + number.getValue());            
            
            match("int");
            
        } else {
            
            throw new Exception("\nError at line " + this.scanner.getLine() + ": invalid arithmetic expression: open parenthesis, int or identifier expected");
        
        }
    }

    private void moreArithmeticFactors() throws Exception {
        if (this.token.getName().equals("multiply")) {
            
            match("multiply");
            
            arithmeticFactor();        
                        
            this.code.generate("*");
            
            moreArithmeticFactors();
        
        } else if (this.token.getName().equals("divide")) {
            
            match("divide");
            
            arithmeticFactor();
                        
            this.code.generate("/");

            moreArithmeticFactors();
        
        } else if (this.token.getName().equals("modulus")) {
            
            match("modulus");
            
            arithmeticFactor();
                        
            this.code.generate("%");            

            moreArithmeticFactors();
            
        }        
    }
    
    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) {
            this.token = this.scanner.getToken();
        } else {
			throw new Exception("\nError at line " + this.scanner.getLine() + ": " + (this.scanner.getLexeme(tokenName).equals("null") ? "token " + tokenName + " is not defined in 'lexicon.txt'" : tokenName + " expected"));
        }
    }
}

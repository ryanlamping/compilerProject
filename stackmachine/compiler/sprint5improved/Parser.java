package stackmachine.compiler.sprint5improved;

import java.util.HashMap;
import java.util.Map;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for declaration of arrays and logic expressions (Sprint 3)
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
 *     optional-declaration     ->  = { generateCode("addressof " + id.lexeme) } logic-expression { generateCode("store") } |
 *                                  [int] |
 *                                  epsilon                     
 *
 *     statements               ->  statement statements |
 *                                  epsilon
 *                              
 *     statement                ->  declaration |
 * 
 *                                  id assignment-expression ; |
 * 
 *                                  if ( logic-expression ) { out = newLabel(); code("gofalse" + out) 
 *                                  statement { code("label" + out) } |
 *                                  
 *                                  if ( logic-expression ) { else = newLabel(); code("gofalse" + else); } 
 *                                  statement { out = newLabel(); code("goto" + out); code("label" + else) } 
 *                                  else statement { code("label" + out) }
 * 
 *                                  while { test = newLabel(); code("label" + test) } 
 *                                  ( logic-expression ) { out = newLabel(); code("gofalse" + out) }
 *                                  statement { code("goto" + test); code("label" + out) }
 * 
 *                                  do { test = newLabel(); code("label" + test) } 
 *                                  statement 
 *                                  while ( logic-expression ) { out = newLabel(); code("gofalse" + out) } 
 *                                  { code("goto" + test); code("label" + out) }
 *                                  
 *                                  print( print-arguments )   { generateCode("println") }   |
 * 
 *                                  println( print-arguments ) { generateCode("printlnln") } |
 * 
 *                                  read( { generateCode("addressof " + id.lexeme); optional-array.id = id }
 * 
 *                                  id optional-array { generateCode("read") ) |
 * 
 *                                  { statements }
 *                              
 *     logic-expression         ->  logic-term more-logic-terms
 *     
 *     logic-term               ->  logic-factor more-logic-factors
 *     
 *     logic-factor             ->  ! logic-expression | true | false | relational-expression
 *     
 *     more-logic-terms         ->  || logic-term { generateCode("||") } more-logic-terms |
 *                                  epsilon
 *                                  
 *     more-logic-factors       ->  && logic-factor { generateCode("&&") } more-logic-factors |
 *                                  epsilon
 *                                  
 *     relational-expression    ->  arithmetic-expression relational-operator arithmetic-expression |
 *                                  arithmetic-expression
 *
 *     relational-operator      ->  < | <= | > | >= | == | !=
 *     
 *     assignment-expression    ->  = { generateCode("addressof " + id.lexeme) } logic-expression { generateCode("store") }                        

 *     optional-array           ->  [ arithmetic-expression ] { generateCode("+") } |
 *                                  epsilon                             
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
 *     arithmetic-factor        ->  base-factor more-powers
 * 
 *     more-powers              -> ** arithmetic-factor { generate code("**") } |
 *                                 epsilon
 * 
 *     base-factor              ->   (arithmetic-expression) |
 *                                   id  { generateCode("addressof " + id.lexeme); generateCode("load") } |
 *                                   int { generateCode("push " + int.value) }
 *  
 *     print-arguments          ->  string { generateCode("literal '" + literal.getValue() + "'") } more-print-arguments |
 *                                  arithmetic-expression { generateCode("ostream") } more-print-arguments
 *                                  
 *     more-print-arguments     ->  , print-arguments |
 *                                  epsilon
 */

 public class Parser implements IParser {
    private IToken token;
    private IScanner scanner;
    private IIntermediateCode code;
    private Map<String, IDataType> symbols;
    private Integer counter;
    
    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new IntermediateCode();
        this.counter = 0;
    }

    @Override
    public String compile() throws Exception {
        program();

        return this.code.toString();
    }

    private String newLabel() {
        return "Label " + this.counter++;
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

			// the token 'assignment' allows to assign a value to a variable in the declaration

			match("assignment");
			
			this.code.generate("addressof " + id.getLexeme());
				
			logicExpression();
				
			this.code.generate("store");
					
		} else if (this.token.getName().equals("open-square-bracket")) {
			
			// the token 'open-square-bracket' declares a one-dimensional array of primitive data types int, float or boolean
			
			match("open-square-bracket");
			
			if (this.token.getName().equals("int")) {
				IntegerNumber number = (IntegerNumber) this.token;
				
				int size = number.getValue();

				this.symbols.put(id.getLexeme(), new OneDimensionalArray(type, size));	
				this.code.generate("array " + id.getLexeme() + " " + type + " " + size);
			}
			
			match("int");
			match("closed-square-bracket");

		}
    }    

    private void statements() throws Exception {
		String tokenName = this.token.getName();

		// check the tokens in FIRST(statement)
		
		if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id") || tokenName.equals("print") || tokenName.equals("println") || tokenName.equals("read") || tokenName.equals("if") || tokenName.equals("while") || tokenName.equals("do") || tokenName.equals("open-curly-bracket")) {			
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
        else if (tokenName.equals("if")) {
			
		    match("if");

		    match("open-parenthesis");

		    logicExpression();

		    match("closed-parenthesis");

            // Creating else Label
		    String elseLabel = newLabel();
		    
            // If false jump to else
		    this.code.generate("gofalse " + elseLabel);

            
		    statement();
		    
            // If there is an else
		    if (this.token.getName().equals("else")) {
		    	
		        String outLabel = newLabel();
		        
                // Jump to out label
		        this.code.generate("goto " + outLabel);
		        
                // Label where else is
		        this.code.generate(elseLabel + ":");

		        match("else");

                statement();
		        
		        this.code.generate(outLabel + ":");
		        
		    } 
            // If no else
            else {
		        this.code.generate(elseLabel + ":");
		    }
		}
        else if (tokenName.equals("while")) {

            // Match while
            match("while");

            // Creating test label
            String test = newLabel();

            // Labeling test point
            this.code.generate(test + ":");

            // Matching grammar
            match("open-parenthesis");

            logicExpression();

            match("closed-parenthesis");

            // Creating exit label
            String outLabel = newLabel();

            // If false, jump to exit
            this.code.generate("gofalse " + outLabel);

            
            statement();

            // Loop back
            this.code.generate("goto " + test);

            // Labeling exit
            this.code.generate(outLabel + ":");
        }
        else if (tokenName.equals("do")) {
		  
		    match("do");

		    String test = newLabel();
		    
			// Start of loop
		    this.code.generate(test + ":");

			statement();

		    match("while");

		    match("open-parenthesis");

		    logicExpression();

		    String out = newLabel();
		    
			// If false loop exit
		    this.code.generate("gofalse " + out);

		    
		    match("closed-parenthesis");

            // If true then go to test
		    this.code.generate("goto " + test);

            // Labeling exit
		    this.code.generate(out + ":");

		    match("semicolon");
        }
        else if (tokenName.equals("print")) {
			
			match("print");
			match("open-parenthesis");
			
			printArguments();

			this.code.generate("print");
			
			match("closed-parenthesis");
			match("semicolon");
			
		} else if (tokenName.equals("println")) {
			
			match("println");
			match("open-parenthesis");
			
			printArguments();

			this.code.generate("println");
			
			match("closed-parenthesis");
			match("semicolon");
			
		} else if (tokenName.equals("read")) {
			
			match("read");
			match("open-parenthesis");
			
            Identifier id = (Identifier) this.token;
            
			if (this.symbols.get(id.getLexeme()) == null) {				
				throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' not declared");
			}
            
            this.code.generate("addressof " + id.getLexeme());

            match("id");
            
			optionalArray(id);
			
			this.code.generate("read");
			
			match("closed-parenthesis");
			match("semicolon");
			
		}
        else if(tokenName.equals("open-curly-bracket")) {
            match("open-curly-bracket");

            statements();

            match("closed-curly-bracket");
        }
    }

    

    // logic-expression         ->  logic-term more-logic-terms
    private void logicExpression() throws Exception {
        logicTerm(); moreLogicTerms();
    }

    // logic-term               ->  logic-factor more-logic-factors
    private void logicTerm() throws Exception {
      logicFactor(); moreLogicFactors();      
    }
    
    // more-logic-terms         ->  || logic-term { generateCode("||") } more-logic-terms |
    //                              epsilon
    private void moreLogicTerms() throws Exception {
      if (this.token.getName().equals("or")) {

        match("or");

        logicTerm();

        this.code.generate("||");

        moreLogicTerms();
      }

      // No else since epsilon

    }
    
    // logic-factor             ->  ! logic-expression | true | false | relational-expression
    private void logicFactor() throws Exception {
    
      if (this.token.getName().equals("not")) {

        match("not");

        logicExpression();

        this.code.generate("!");

      } else if (this.token.getName().equals("true")){
        
        this.code.generate("push 1");

        match("true");

      } else if (this.token.getName().equals("false")) {
        
        this.code.generate("push 0");

        match("false");

      } else {
        relationalExpression();
      }
   
    }
    
    // more-logic-factors       ->  && logic-factor { generateCode("&&") } more-logic-factors |
    //                              epsilon
    private void moreLogicFactors() throws Exception {
      
        if(this.token.getName().equals("and")) {
        
            match("and");
    
            logicFactor();
    
            this.code.generate("&&");
    
            moreLogicFactors();
        }
    
        // No else since epsilon

    }
    

    // relational-expression    ->  arithmetic-expression relational-operator arithmetic-expression |
    //                              arithmetic-expression
	private void relationalExpression() throws Exception {
		arithmeticExpression();
		
		String tokenName = this.token.getName();
		
		if (tokenName.equals("greater-than") || tokenName.equals("greater-or-equal") || tokenName.equals("less-than") || tokenName.equals("less-or-equal") || tokenName.equals("equal") || tokenName.equals("not-equal")) {
			
			String operator = this.scanner.getLexeme(tokenName);
			
			match(tokenName);
			
			arithmeticExpression();
			
			this.code.generate(operator);
			
		}
	}

    private void assignmentExpression() throws Exception {        
        Identifier id = (Identifier) this.token;
        
        if (this.symbols.get(id.getLexeme()) == null) {                
           throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' not declared");
        }
        
        this.code.generate("addressof " + id.getLexeme());
        
        match("id");
        
        optionalArray(id);
        
        match("assignment");
        
        logicExpression();
        
        this.code.generate("store");
    }

	private void optionalArray(Identifier id) throws Exception {
		if (this.token.getName().equals("open-square-bracket")) {
				
			match("open-square-bracket");
				
			arithmeticExpression();
				
			match("closed-square-bracket");
				
			// the value of the arithmetic expression is added to the base address of the array

			this.code.generate("+");
		}
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
        baseFactor(); morePowers();
    }

    private void morePowers() throws Exception {
        if (this.token.getName().equals("power")) {

            match("power");
            
            // arithmeticFactor instead of baseFactor
            arithmeticFactor();

            this.code.generate("**");
        }
    }

    private void baseFactor() throws Exception {
        if (this.token.getName().equals("open-parenthesis")) {
            
            match("open-parenthesis");
            
            arithmeticExpression();
            
            match("closed-parenthesis");
        
        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;
            
            if (this.symbols.get(id.getLexeme()) == null) {                
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' not declared");
            }
            
            this.code.generate("addressof " + id.getLexeme());

            match("id");
            
            optionalArray(id);

            this.code.generate("load");

        } else if (this.token.getName().equals("int")) {
            
            IntegerNumber number = (IntegerNumber) this.token;
                            
            this.code.generate("push " + number.getValue());            
            
            match("int");
            
        } else if (this.token.getName().equals("float")) {
            
            FloatingPointNumber number = (FloatingPointNumber) this.token;
            
            this.code.generate("push " + number.getValue());
            
            match("float");

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
    
	private void printArguments() throws Exception {
		if (this.token.getName().equals("string")) {

			StringLiteral literal = (StringLiteral) this.token;

			match("string");

			this.code.generate("literal '" + literal.getValue() + "'");

			morePrintArguments();

		} else {
			
			arithmeticExpression();
         
			this.code.generate("ostream");
         
			morePrintArguments();

		}
	}
	
	private void morePrintArguments() throws Exception {
		if (this.token.getName().equals("comma")) {			
			match("comma");
		
			printArguments();
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
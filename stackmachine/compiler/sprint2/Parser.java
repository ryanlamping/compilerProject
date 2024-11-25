package stackmachine.compiler.sprint2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for declaration of variables (Sprint 2)
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
 *                                  { addSymbol(id.lexeme, identifiers.type); assignment-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                                  assignment-declaration
 *                                  more-identifiers
 *                                      
 *     more-identifiers         ->  , id 
 *                                  { addSymbol(id.lexeme, identifiers.type); assignment-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                                  assignment-declaration
 *                                  more-identifiers |
 *                                  epsilon
 *                           
 *     assignment-declaration   ->  = { generateCode("addressof " + id.lexeme) } expression { generateCode("store") } |
 *                                  epsilon                     
 *  
 *  Syntax-directed definition for arithmetic expressions (Sprint 2)
 *
 *     statements               ->  statement statements |
 *                                  epsilon
 *                              
 *     statement                ->  declaration     |
 *                                  id assignment-expression ;
 *                              
 *     assignment-expression    ->  = { generateCode("addressof " + id.lexeme) } arithmetic-expression { generateCode("store") }
 *     
 *     arithmetic-expression    ->  arithmetic-expression + arithmetic-term { generateCode("+") } |
 *                                  arithmetic-expression - arithmetic-term { generateCode("-") } |
 *                                  arithmetic-term
 *                  
 *     arithmetic-term          ->  arithmetic-term * arithmetic-factor { generateCode("*") } |
 *                                  arithmetic-term / arithmetic-factor { generateCode("/") } |
 *                                  arithmetic-term % arithmetic-factor { generateCode("%") } |
 *                                  arithmetic-factor
 *                  
 *     arithmetic-factor        ->  (arithmetic-expression)                                      |
 *                                  id  { generateCode("addressof " + id.lexeme); generateCode("load") } |
 *                                  num { generateCode("push " + num.value) }
 *                  
 *  Right-recursive SDD for arithmetic expressions (Sprint 2)
 *
 *     statements               ->  statement statements |
 *                                  epsilon
 *                              
 *     statement                ->  declaration |
 *                                  id assignment-expression ; |
 *                                  print(print-arguments)
 *                              
 *     assignment-expression    ->  id = { generateCode("addressof " + id.lexeme) } arithmetic-expression { generateCode("store") }
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
 *                                   num { generateCode("push " + num.value) }
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
        // Save variables in HashMap with name of id (String), and type (IDataType)
        this.symbols = new HashMap<String, IDataType>();
        // Keep track of code (instructions) as we parse the tree
        this.code = new IntermediateCode();
    }

    public String compile() throws Exception {
        program();

        // Return full code from parse tree
        return this.code.toString();
    }
    
    private void program() throws Exception {
        // Match necessary tokens to begin program
        match("void");
        match("main");
        match("open-curly-bracket");
        
        // Following grammar rule
        declarations();
        statements();
        
        match("closed-curly-bracket");
        
        this.code.generate("halt");
    }
    
    private void declarations() throws Exception {
        // Checking to see if variable is being declared
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
        // No else because epsilon in grammar rules
    }

    private void declaration() throws Exception {
        // Confirm id
        identifiers(type());
        // Match semicolon at end of declaration
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

              if (this.symbols.get(id.getLexeme()) == null)
                  this.symbols.put(id.getLexeme(), new PrimitiveType(type));
              else
                   throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
        
              match("id");
            
              assignmentDeclaration(type, id);
            
              moreIdentifiers(type);
         } else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");
        }
    }
    
    private void moreIdentifiers(String type) throws Exception {
        // If more than one of same variable type is declared
         if (this.token.getName().equals("comma")) {
              match("comma");
            
              if (this.token.getName().equals("id")) {
                  Identifier id = (Identifier) this.token;

                   if (this.symbols.get(id.getLexeme()) == null)
                        this.symbols.put(id.getLexeme(), new PrimitiveType(type));
                   else
                       throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

                   match("id");
                
                   assignmentDeclaration(type, id);

                   moreIdentifiers(type);
              } else {
                   throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier expected");                
              }
         }
    }
    
    private void assignmentDeclaration(String type, Identifier id) throws Exception {
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
          } 
          else if (tokenName.equals("id")) {
            assignmentExpression();
            match("semicolon");
          }
    }

    private void assignmentExpression() throws Exception {  
        Identifier id = (Identifier) this.token;

        this.code.generate("address of" + id.getLexeme());

        match("id");
        match("assignment");

        arithmeticExpression();

        this.code.generate("store");
    }

    // arithmetic-expression    ->  arithmetic-expression + arithmetic-term { generateCode("+") } |
    //  *                                  arithmetic-expression - arithmetic-term { generateCode("-") } |
    //  *                                  arithmetic-term
    private void arithmeticExpression() throws Exception {
        if(this.token.getName().equals("+")) {
            arithmeticExpression();
            arithmeticTerm();
            this.code.generate("+");
            match("+");
        }
        else if(this.token.getName().equals("-")) {
            arithmeticExpression();
            arithmeticTerm();
            this.code.generate("-");
            match("-");
        }
        else {
            arithmeticTerm();
        }

    }
        
    // arithmetic-term          ->  arithmetic-factor more-arithmetic-factors
    private void arithmeticTerm() throws Exception {
       
       arithmeticFactor();
       moreArithmeticFactors();
       
    }
        
    // *     more-arithmetic-terms    ->  + arithmetic-term { generateCode("+") } more-arithmetic-terms |
    // *                                  - arithmetic-term { generateCode("-") } more-arithmetic-terms |
    // *                                  epsilon
    private void moreArithmeticTerms() throws Exception {
       
        if(this.token.getName().equals("add")) {
            this.code.generate("+");

            moreArithmeticTerms();

            match("+");
        }
        else if(this.token.getName().equals("subtract")) {
            this.code.generate("-");

            moreArithmeticTerms();
            
            match("-");
        }
        // Else epsilon so not else
    }

    private void arithmeticFactor() throws Exception {
        if (this.token.getName().equals("open-parenthesis")) {

            match("open-parenthesis");
            arithmeticExpression();
            match("closed-parenthesis");
        }

       // else if it is an id
       // id  { generateCode("addressof " + id.lexeme); generateCode("load") }

       else if (this.token.getName().equals("id")) {

            // Downcast into Identifier
            Identifier id = (Identifier) this.token;

            // Get lexeme
            this.code.generate("addressof " + id.getLexeme());
            this.code.generate("load");

            match("id");
        }

       // else if it is a num
       // num { generateCode("push " + num.value) }

       else if(this.token.getName().equals("int")){
            // Downcast into IntegerNumber
            IntegerNumber num = (IntegerNumber) this.token;

            // generate code
            this.code.generate("push" + num.getValue());

            match("int");
       }
       else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": factor expected");
       }
    }

    // *     more-arithmetic-factors  ->  * arithmetic-factor { generateCode("*") } more-arithmetic-factors |
    // *                                  / arithmetic-factor { generateCode("/") } more-arithmetic-factors |
    // *                                  % arithmetic-factor { generateCode("%") } more-arithmetic-factors |
    // *                                  epsilon
    private void moreArithmeticFactors() throws Exception {
       
       // implement the grammar rule more-arithmetic-factors
    }
    
    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) {
            this.token = this.scanner.getToken();
        } else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + (this.scanner.getLexeme(tokenName).equals("null") ? "token " + tokenName + " is not defined in 'lexicon.txt'" : tokenName + " expected"));
        }
    }
}
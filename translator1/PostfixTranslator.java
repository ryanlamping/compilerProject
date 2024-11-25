package translator1;

import slu.compiler.*;

/* 
 *  Syntax-directed definition to translate infix arithmetic expressions into postfix notation
 *  
 *     translator  -> { postfix = "" } expression { print(postfix) }
 *     
 *     expression  -> expression + term { postfix = postfix + "+" } |
 *                    expression - term { postfix = postfix + "-" }
 *                    term
 *                  
 *     term        -> term * factor { postfix = postfix + "*" } |
 *                    term / factor { postfix = postfix + "/" } |
 *                    term % factor { postfix = postfix + "%" } |
 *                    factor
 *                  
 *     factor      -> (expression) |
 *                    int { postfix = postfix + int.value }
 *                  
 *  Right-recursive SDD for a top-down recursive predictive parser
 *
 *     translator  -> { postfix = "" } expression { print(postfix) }
 *
 *     expression  -> term moreTerms
 *     
 *     moreTerms   -> + term { postfix = postfix + "+" } moreTerms |
 *                    - term { postfix = postfix + "-" } moreTerms |
 *                    epsilon
 *               
 *     term        -> factor moreFactors
 *     
 *     moreFactors -> * factor { postfix = postfix + "*" } moreFactors |
 *                    / factor { postfix = postfix + "/" } moreFactors |
 *                    % factor { postfix = postfix + "%" } moreFactors |
 *                    epsilon
 *                    
 *     factor      -> (expression) |
 *                    int { postfix = postfix + int.value }
 *  
 *  
 *  The expression 9 - 5 + 2 * 3 is translated into 9 5 - 2 3 * +
 *  
 */

public class PostfixTranslator implements IPostfixTranslator {
    private IToken token;
    private IScanner scanner;
    private String postfix;
    
    public PostfixTranslator(IScanner lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
    }

    @Override
    public String translate() throws Exception {
        this.postfix = "";
        
        expression();
        
        return this.postfix;
    }
    
    private void expression() throws Exception {
        term(); moreTerms();
    }
    
    // term --> factor more-factors

    private void term() throws Exception {
        factor(); moreFactors();
    }
        
    // pop factors from stack and operate with operator found
    private void moreTerms() throws Exception {
        if (this.token.getName().equals("add")) {
            
            match("add");
            
            term();
                        
            this.postfix = this.postfix + " + ";
            
            moreTerms();

        } else if (this.token.getName().equals("subtract")) {
            
            match("subtract");
            
            term();

            this.postfix = this.postfix + " - ";
            
            moreTerms();
        } 
    }

    // push factor into stack
    private void factor() throws Exception {
        if (this.token.getName().equals("open-parenthesis")) {

            match("open-parenthesis");
            expression();
            match("closed-parenthesis");
        }
        else if (this.token.getName().equals("int")) {
            // Declare a variable number of type IntegerNumber to get the value of a token "int"
            // Down Cast IToken --> Integer Number 
            IntegerNumber number = (IntegerNumber) this.token;

            // The value of the token is given by the method getValue()
            // Add value of token to the Class postfix string
            // push into stack
            this.postfix = this.postfix + number.getValue() + " ";

            match("int");
        }
        // factor does not produce epsilon so throw error if no match
        else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ", open parenthesis or int expected");
        }
    }

    // pop factors from stack and operate with operator found
    // produces four rules (like more terms, but * factor instead of + terms)
    private void moreFactors() throws Exception {
        if (this.token.getName().equals("multiply")) {
            match("multiply");

            factor();

            this.postfix = this.postfix + " * ";
            
            moreFactors();
        }
        else if(this.token.getName().equals("divide")) {
            match("divide");

            factor();

            this.postfix = this.postfix + " / ";

            moreFactors();
        }
        else if (this.token.getName().equals("modulus")) {
            match("modulus");

            factor();

            this.postfix = this.postfix + " % ";

            moreFactors();
        }
    }
    
    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) {
            // move onto the next token
            this.token = this.scanner.getToken();
        } 
        // throw error as source code is incorrect
        else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + (this.scanner.getLexeme(tokenName).equals("null") ? "token " + tokenName +
                                " is not defined in 'lexicon.txt'" : tokenName + " expected"));
        }
    }
}
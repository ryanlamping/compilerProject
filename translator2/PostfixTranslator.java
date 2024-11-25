package translator2;

import java.util.Stack;

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
    private Stack<Integer> evaluator = new Stack<>();
    
    // based off token when parsing, add or pop to stack
    // come across operator --> operate
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
            this.postfix = this.postfix + number.getValue() + " ";

            match("int");
        }
        else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ", open parenthesis or int expected");
        }
    }

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
            this.token = this.scanner.getToken();
        } else {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + (this.scanner.getLexeme(tokenName).equals("null") ? "token " + tokenName +
                                " is not defined in 'lexicon.txt'" : tokenName + " expected"));
        }
    }

    // Boolean function to check if the character is an operator
    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%';
    }

    // Return the result of postfix operation
    // Two numbers get popped from stack
    // Operator being detected from string triggers the function
    private int operate(int num1, int num2, char operator) throws Exception {
        switch (operator) {
            case '+': return num1 + num2;
            case '-': return num1 - num2;
            case '*': return num1 * num2;
            case '/': if (num2 == 0) throw new Exception("Division by zero");
                      return num1 / num2;
            case '%': return num1 % num2;
        default: throw new Exception("Unsupported operator: " + operator);
        }
    }

    // literally just pop stack
    // if character is an operator --> pop stack
    // operate with stack content and operator
    // push result to stack

    public int evaluate() throws Exception {

        System.out.println("postfix string: " + this.postfix);
        // Iterator to go through postfix
        int i = 0;

        while(i < this.postfix.length()) {
            // Save current character
            char ch = this.postfix.charAt(i);

            // Move to next character if it is a space
            if (ch == ' ') {
                i++;
                continue;
            }

            // If it is a digit --> take into account potential numbers with multiple digits
            else if (Character.isDigit(ch)) {
                int num = 0;

                while(i < this.postfix.length() && Character.isDigit(this.postfix.charAt(i))) {

                    // Multiply current value by 10 and add new character converted to an int
                    // How to convert a char to an int: charAt(i) - '0'
                    num = (num * 10) + (this.postfix.charAt(i) - '0');

                    // Increment i to see if next character is also an int
                    i++;
                }
                // Push into stack and move onto next character in postfix string
                System.out.println("Pushing number into stack: " + num);
                evaluator.push(num);
            }

            else if (isOperator(ch)) {
                System.out.println("Operator trigger: " + ch);
                int num2 = evaluator.pop();
                int num1 = evaluator.pop();

                int result = operate(num1, num2, ch);
                System.out.println("Result into stack: " + result);
                evaluator.push(result);

                i++;
            }

            else {
                throw new Exception("Invalid character in expression: " + ch);
            }
        }
        // Return the last int in the stack
        return evaluator.pop();
    }
}
package translator2;

import slu.compiler.*;

public class TestProgram {

    public static void main(String[] args) {
        IToken tokenName;
        boolean showTokens = false;
        
        String expression = "((10 * 3) + (50 / 5)) * 2 / 4";

        try {
            
            IScanner scanner = new Scanner(expression);
    
            if (showTokens) {
                do {
                    tokenName = scanner.getToken();
                    
                    System.out.println("<" + tokenName.toString() + ">");
                    
                } while (!tokenName.getName().equals("null"));
    
                System.out.println("");            
            }
            
            IPostfixTranslator postfix = new PostfixTranslator( new Scanner(expression) );
            
            System.out.println("Infix expression   " + expression);
            System.out.println("Postfix expression " + postfix.translate());
            System.out.println("Evaluated postfix expression " + postfix.evaluate());
            
        } catch (Exception e) {
            System.out.println(e.getMessage());            
        }    
    }
}
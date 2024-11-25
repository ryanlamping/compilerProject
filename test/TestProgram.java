package test;

import slu.compiler.*;

public class TestProgram {

    public static void main(String[] args) {
        IToken tokenName;
        
        String expression =  "10 3 * 5 +"; // "((10 * 3) + (50 / 5)) * 2 / 4";

        try {
            
            IScanner scanner = new Scanner(expression);
    
            do {
                tokenName = scanner.getToken();
                    
                System.out.println("<" + tokenName.toString() + ">");
                    
            } while (!tokenName.getName().equals("null"));
                
        } catch (Exception e) {
            System.out.println(e.getMessage());            
        }    
    }
}

// if we update with match("int") it would modify the token so always do it at the end of the if


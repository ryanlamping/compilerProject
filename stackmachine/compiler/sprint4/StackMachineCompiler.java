package stackmachine.compiler.sprint4;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import slu.compiler.*;

public class StackMachineCompiler implements IStackMachineCompiler {
    private IParser parser;

    @Override
    public void compile(String program, String fileName) throws Exception {
        try {

            this.parser = new Parser( new Scanner(program, StandardCharsets.UTF_8) );

            String code = this.parser.compile();        
            
            PrintWriter outputFile = new PrintWriter(fileName);
        
            outputFile.print(code);        
            outputFile.close();
            
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    
}
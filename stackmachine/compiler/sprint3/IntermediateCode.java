package stackmachine.compiler.sprint3;

import java.util.ArrayList;
import java.util.List;

public class IntermediateCode implements IIntermediateCode {
    private List<String> code;
    
    public IntermediateCode() {
        this.code = new ArrayList<String>();
    }
    
    @Override
    public void generate(String code) {
        this.code.add(code);
    }

    @Override
    public String toString() {
        String code = "";
        
        for (String instruction : this.code) {
            code = code + instruction + "\n";
        }
        
        return code;
    }
}
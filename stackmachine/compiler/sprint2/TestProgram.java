package stackmachine.compiler.sprint2;

public class TestProgram {

	public static void main(String[] args) {
		try {					
			IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
			
			stackMachineCompiler.compile("program test assignment.txt", "sm test assignment.txt");
			
			System.out.println("'program test assignment.txt' compiled succesfully!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}	
	}
}
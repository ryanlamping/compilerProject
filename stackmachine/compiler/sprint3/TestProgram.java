package stackmachine.compiler.sprint3;

public class TestProgram {

	public static void main(String[] args) {
		try {					
			IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
			
			stackMachineCompiler.compile("sprint4 test file.txt", "sm test assignment.txt");
			
			System.out.println("'program test assignment.txt' compiled succesfully!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}	
	}
}
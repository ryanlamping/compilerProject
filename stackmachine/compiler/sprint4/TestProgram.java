package stackmachine.compiler.sprint4;

public class TestProgram {

	public static void main(String[] args) {
		try {					
			IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
			
			stackMachineCompiler.compile("sprint4 test file.txt", "sprint4 output.txt");
			
			System.out.println("'sprint4 test file.txt' compiled succesfully!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}	
	}
}
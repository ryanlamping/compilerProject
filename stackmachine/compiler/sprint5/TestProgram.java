package stackmachine.compiler.sprint5;

public class TestProgram {

	public static void main(String[] args) {
		try {					
			IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
			
			stackMachineCompiler.compile("program binary search boolean.txt", "sm binary search boolean.txt");
			
			System.out.println("'sm factorial 10.txt' compiled succesfully!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}	
	}
}
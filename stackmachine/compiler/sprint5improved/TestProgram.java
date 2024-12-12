package stackmachine.compiler.sprint5improved;

public class TestProgram {

	public static void main(String[] args) {
		try {					
			IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
			
			stackMachineCompiler.compile("program Newton sqrt 2 do while.txt", "sm Newton sqrt 2 do while.txt");
			
			System.out.println("'sm factorial 10.txt' compiled succesfully!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}	
	}
}
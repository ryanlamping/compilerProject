package stackmachine.stackmachine;

import slu.stackmachine.*;


public class TestProgram {

	public static void main(String[] args) {

		try {
						
			IStackMachine stackMachine = new StackMachine();

			stackMachine.run("sprint4 output.txt");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}

/**
 * The HelloWorldApp class implements an application that
 * simply prints "Hello World!" to standard output.
 */
class HelloWorldApp {
	public static void main(String[] args) {
		int[][] data = new int[3][3];
		data[0][0] = 1;
		String str = "abcdefghij";
		System.out.println("Hello World!"); // Display the string.
		System.out.println(str.substring(2,3));
		System.out.println(data[0][0]);
	}
}

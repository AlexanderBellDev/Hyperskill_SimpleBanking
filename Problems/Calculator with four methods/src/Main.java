// Don't delete this import statement

import java.util.Scanner;

class SimpleCalculator {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        long num1 = scanner.nextLong();
        String operator = scanner.next();
        long num2 = scanner.nextLong();

        switch (operator) {
            case "+":
                sumTwoNumbers(num1, num2);
                break;
            case "/":
                divideTwoNumbers(num1, num2);
                break;
            case "*":
                multiplyTwoNumbers(num1, num2);
                break;
            case "-":
                subtractTwoNumbers(num1, num2);
                break;
        }

    }

    // Implement your methods here
    public static void subtractTwoNumbers(long first, long second) {
        System.out.println(first - second);
    }


    public static void sumTwoNumbers(long first, long second) {
        System.out.println(first + second);
    }


    public static void divideTwoNumbers(long first, long second) {
        if (second == 0) {
            System.out.println("Division by 0!");
        } else {
            System.out.println(first / second);
        }
    }


    public static void multiplyTwoNumbers(long first, long second) {
        System.out.println(first * second);
    }
}
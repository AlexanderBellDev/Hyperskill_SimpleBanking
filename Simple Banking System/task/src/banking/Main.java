package banking;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static List<Account> accountList = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static String url;
    private static int balance;

    public static void main(String[] args) {
        String dbName = args[1];
        url = "jdbc:sqlite:" + dbName;
        if (!new File(dbName).exists()) {
            createDb();
        } else {
            readValuesFromDb();
        }

        while (true) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");

            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) {
                System.out.println("Bye!");
                System.exit(0);
            }
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    Optional<Account> account = logIn();
                    account.ifPresent(Main::authenticatedUserMenu);
            }
        }
    }

    private static void readValuesFromDb() {
        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                ResultSet resultSet = statement.executeQuery("SELECT number,pin FROM card;");
                while (resultSet.next()) {
                    String cardNumber = resultSet.getString("number");
                    String pin = resultSet.getString("pin");
                    accountList.add(new Account(cardNumber, pin));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDb() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL," +
                        "balance INTEGER DEFAULT 0)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void authenticatedUserMenu(Account authenticatedAccount) {
        System.out.println("You have successfully logged in!");
        System.out.println();
        loop:
        while (true) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) {
                System.out.println("Bye!");
                System.exit(0);
            }
            switch (choice) {
                case 1:
                    printBalance(authenticatedAccount);
                    break;
                case 2:
                    addIncome(authenticatedAccount);
                    break;
                case 3:
                    transfer(authenticatedAccount);
                    break;
                case 4:
                    closeAccount(authenticatedAccount);
                    break loop;
                case 5:
                    logOut();
                    break loop;
            }
        }
    }

    private static void closeAccount(Account authenticatedAccount) {
        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("DELETE FROM card WHERE number = " + authenticatedAccount.getCardNumber());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("The account has been closed!");
        System.out.println();
    }

    private static void transfer(Account authenticatedAccount) {
        System.out.println();
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        boolean flag = false;
        String inputCardNumber = scanner.nextLine();
        if (validateCreditCardNumber(inputCardNumber)) {
            if (inputCardNumber.equals(authenticatedAccount.getCardNumber())) {
                System.out.println("You can't transfer money to the same account!");
                System.out.println();
            } else {
                try (Connection con = DriverManager.getConnection(url)) {
                    // Statement creation
                    try (Statement statement = con.createStatement()) {
                        // Statement execution
                        ResultSet resultSet = statement.executeQuery("SELECT number FROM card WHERE number =" + inputCardNumber);
                        if (resultSet.next()) {
                            flag = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (!flag) {
                    System.out.println("Such a card does not exist");
                    System.out.println();
                } else {
                    System.out.println("Enter how much money you want to transfer:");
                    int amountToTransfer = Integer.parseInt(scanner.nextLine());
                    updateBalance(authenticatedAccount);
                    if (amountToTransfer > balance) {
                        System.out.println("Not enough money!");
                        System.out.println();
                    } else {
                        try (Connection con = DriverManager.getConnection(url)) {
                            // Statement creation
                            try (Statement statement = con.createStatement()) {
                                // Statement execution
                                statement.executeUpdate("UPDATE card SET balance = balance -" + amountToTransfer + " WHERE number = " + authenticatedAccount.getCardNumber());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        try (Connection con = DriverManager.getConnection(url)) {
                            // Statement creation
                            try (Statement statement = con.createStatement()) {
                                // Statement execution
                                statement.executeUpdate("UPDATE card SET balance = balance +" + amountToTransfer + " WHERE number = " + inputCardNumber);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Success!");
                        System.out.println();
                    }
                }
            }
        } else {
            System.out.println("Probably you made mistake in the card number. Please try again!");
            System.out.println();
        }


    }

    private static void addIncome(Account authenticatedAccount) {

        System.out.println();
        System.out.println("Enter income:");
        int amountToAdd = Integer.parseInt(scanner.nextLine());
        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("UPDATE card SET balance = balance +" + amountToAdd + " WHERE number = " + authenticatedAccount.getCardNumber());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Income was added!");
        System.out.println();
    }

    private static void logOut() {
        System.out.println();
        System.out.println("You have successfully logged out!");
        System.out.println();
    }

    private static void printBalance(Account authenticatedAccount) {
        updateBalance(authenticatedAccount);

        System.out.println();
        System.out.println("Balance: " + balance);
        System.out.println();
    }

    private static void updateBalance(Account authenticatedAccount) {
        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                ResultSet resultSet = statement.executeQuery("SELECT * FROM card WHERE number =" + authenticatedAccount.getCardNumber());
                while (resultSet.next()) {
                    balance = resultSet.getInt("balance");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Account> logIn() {
        System.out.println();
        System.out.println("Enter your card number:");
        String loginCardNumber = scanner.nextLine();
        Optional<Account> filteredAccount = accountList.stream()
                .filter(account -> account.getCardNumber().equals(loginCardNumber))
                .findFirst();
        if (filteredAccount.isEmpty()) {
            System.out.println();
            System.out.println("Wrong card number or PIN!");
            System.out.println();
            return filteredAccount;
        } else {
            System.out.println("Enter your PIN:");
            String loginPin = scanner.nextLine();
            if (filteredAccount.get().getPin().equals(loginPin)) {
                System.out.println();
                return filteredAccount;
            } else {
                System.out.println();
                System.out.println("Wrong card number or PIN!");
                System.out.println();
            }
        }
        return Optional.empty();
    }

    private static void createAccount() {
        System.out.println();
        int IIN = 400000;
        int accountNum = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);
        String cardNumWithoutChecksum = String.valueOf(IIN) + accountNum;
        String cardNumber = calcChecksum(cardNumWithoutChecksum);
        String pin = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        Account createdAccount = new Account(cardNumber, pin);
        accountList.add(createdAccount);
        saveAccountToDb(createdAccount);
        System.out.println("Your card has been created");
        System.out.println("Your card number: \n" + createdAccount.getCardNumber());
        System.out.println("Your card PIN: \n" + createdAccount.getPin());
        System.out.println();
    }

    private static void saveAccountToDb(Account createdAccount) {
        try (Connection con = DriverManager.getConnection(url)) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("INSERT INTO card (number, pin) " +
                        "VALUES (" + createdAccount.getCardNumber() + "," + createdAccount.getPin() + ")");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String calcChecksum(String cardNumWithoutChecksum) {
        List<Integer> cardNumDigits = new ArrayList<>();
        for (char c : cardNumWithoutChecksum.toCharArray()) {
            int digit = Character.getNumericValue(c);
            cardNumDigits.add(digit);
        }
        for (int i = 0; i < cardNumDigits.size(); i++) {
            //modify all odd index's by 2 (assume first index is 1)
            if ((i + 1) % 2 != 0) {
                cardNumDigits.set(i, cardNumDigits.get(i) * 2);
            }
        }
        for (int i = 0; i < cardNumDigits.size(); i++) {
            //subtract 9 to numbers over 9
            if (cardNumDigits.get(i) > 9) {
                cardNumDigits.set(i, cardNumDigits.get(i) - 9);
            }
        }
        int total = cardNumDigits.stream().mapToInt(Integer::intValue).sum();
        int checksum = 0;
        if (total % 10 != 0) {
            checksum = 10 - total % 10;
        }
        return cardNumWithoutChecksum + checksum;
    }

    private static boolean validateCreditCardNumber(String str) {

        int[] ints = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            ints[i] = Integer.parseInt(str.substring(i, i + 1));
        }
        for (int i = ints.length - 2; i >= 0; i = i - 2) {
            int j = ints[i];
            j = j * 2;
            if (j > 9) {
                j = j % 10 + 1;
            }
            ints[i] = j;
        }
        int sum = 0;
        for (int anInt : ints) {
            sum += anInt;
        }
        return sum % 10 == 0;
    }

}

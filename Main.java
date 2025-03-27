package banking;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static BankingSystemDB bankingSystemDB;
    private static int balance;
    static String strUserAccountNumber;
    static String strUserCardPIN;

    public static int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    static Scanner sc = new Scanner(System.in);
    static Map<String, String> accountList = new HashMap<>();

    public static void main(String[] args) {
        String dbFileName = "";
        if (args.length < 2 || !(args[0].equals("-fileName"))) {
            System.out.println("Enter the correct file name!");
//            System.exit(1);
        } else {
            dbFileName = args[1];
        }
        bankingSystemDB = new BankingSystemDB(dbFileName);
        menuUserAccount();
    }

    public static void menuUserAccount() {
        try {
            bankingSystemDB.connectToDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            System.out.print("""
                    1. Create an account
                    2. Log into account
                    0. Exit
                    """);
            int userInput = sc.nextInt();
            switch (userInput) {
                case 1 -> createAnAccount();
                case 2 -> {
                    if (BankingSystemDB.logIntoAccount()) {
                        loginMenu();
                    } else {
                        menuUserAccount();
                    }
                }
                case 0 -> {
                    exitMenuUserAccount();
                    return;
                }
                default -> sc.next();
            }
        }
    }

    //Create an account
    public static void createAnAccount() {
        try {
            String key = getAccountIdWithLuhn();///
            String value = codePin();
            bankingSystemDB.addNewCardInDB(key, value);
            accountList.put(key, value);
            System.out.println("""
                    \nYour card has been created
                    Your card number:
                    """ + key +
                    """
                            \nYour card PIN:
                            """ + value);
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Failed to create an account: " + e.getMessage());
        }
    }

    public static String bankIdentificationNumber() {
        final String FIRST_DIGIT_BIN = "4";
        int nextDigitsBIN = ThreadLocalRandom.current().nextInt(0, 99999);
        String strNextDigitsBINFormat = String.format("%05d", nextDigitsBIN).trim();
        //return FIRST_DIGIT_BIN + strNextDigitsBINFormat;
        return "400000";
    }

    public static String accountIdentifier() {
        long accountIdentifier = ThreadLocalRandom
                .current()
                .nextLong(100_000_000L, 999_999_999L);
        return String.format("%09d", accountIdentifier).trim();
    }

    public static String accountNumber() {
        return bankIdentificationNumber() + accountIdentifier();
    }

    public static String codePin() {
        int pinCode = ThreadLocalRandom.current().nextInt(0, 9999);
        return String.format("%04d", pinCode);
    }

    public static void exitMenuUserAccount() {
        System.out.println("Bye!");
        System.exit(0);
    }

    public static void loginMenu() {
        while (true) {
            System.out.print("""
                    1. Balance
                    2. Add income
                    3. Do transfer
                    4. Close account
                    5. Log out
                    0. Exit
                    """);

            int optionNumber = sc.nextInt();
            if (optionNumber >= 0 && optionNumber <= 5) {
                switch (optionNumber) {
                    case 1 -> bankingSystemDB.showBalance();
                    case 2 -> bankingSystemDB.addFunds();
                    case 3 -> bankingSystemDB.doTransfer();
                    case 4 -> {
                        bankingSystemDB.closeAccount();
                        menuUserAccount();
                    }
                    case 5 -> {
                        logOut();
                        return;
                    }
                    case 0 -> {
                        exitUserAccount();
                        return;
                    }
                }
            } else {
                sc.nextLine();
            }
        }
    }

    public static void logOut() {
        System.out.println("\nYou have successfully logged out!\n");
        menuUserAccount();
    }

    public static void exitUserAccount() {
        exitMenuUserAccount();
    }

    public static String getAccountIdWithLuhn() {
        String strOriginalNumber = accountNumber();
        int luhnNumber = 0;
        int digit;
        for (int i = 0; i < strOriginalNumber.length(); i++) {
//            digit = Integer.parseInt(String.valueOf(strOriginalNumber.charAt(i)));
            digit = strOriginalNumber.charAt(i) - '0';//string conversion in a number
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            luhnNumber += digit;
        }
        int checkDigit = (10 - (luhnNumber % 10)) % 10;
        return strOriginalNumber + checkDigit;//auto conversion int -> String
    }

    public static boolean isValidLuhn(String number) {
        if (number.isEmpty()) {
            return false;
        }
        int lastDigit = number.charAt(number.length() - 1) - '0';
        int luhnNumber = 0;
        int digit;
        int checkDigit;
        for (int i = 0; i < number.length() - 1; i++) {
            digit = number.charAt(i) - '0';
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            luhnNumber += digit;
        }
        checkDigit = (10 - (luhnNumber % 10)) % 10;
        return lastDigit == checkDigit;
    }
}
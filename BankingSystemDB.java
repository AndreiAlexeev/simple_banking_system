package banking;

import java.sql.*;
import java.util.Scanner;

import org.sqlite.SQLiteDataSource;

import static banking.Main.*;

public class BankingSystemDB {
    String fileName;
    String url;
    static Scanner sc = new Scanner(System.in);

    public BankingSystemDB(String fileName) {
        this.fileName = fileName;
    }

    public String buildDatabaseUrl() {
        //url = "jdbc:sqlite:/home/andrei/IdeaProjects/Simple Banking System (Java)/" + fileName + ".db";
        url = "jdbc:sqlite:" + fileName;
        return url;
    }

    public void connectToDatabase() throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(buildDatabaseUrl());

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0" +
                        ");");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewCardInDB(String accountNumber, String pinCode) throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(buildDatabaseUrl());

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO card (number, pin) VALUES" +
                        "('" + accountNumber + "', '" + pinCode + "');");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Log into account
    public static boolean logIntoAccount() {
        //sc.nextLine();//am curatat tamponul
        System.out.println("\nEnter your card number: ");

        long userAccountNumber = sc.nextLong();
        sc.nextLine();
        strUserAccountNumber = String.format("%016d", userAccountNumber).trim();
        System.out.println("Enter your PIN: ");
        int userCardPIN = sc.nextInt();
        sc.nextLine();//clean the buffer
        strUserCardPIN = String.format("%04d", userCardPIN).trim();
        //iterez peste accountList
        String select = "SELECT * FROM card WHERE number = ? AND pin = ?";
        boolean isAuthenticated = true;
        try (Connection connection = DriverManager.getConnection(bankingSystemDB.url);
             PreparedStatement preparedStatement = connection.prepareStatement(select)) {
            preparedStatement.setString(1, strUserAccountNumber);
            preparedStatement.setString(2, strUserCardPIN);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("\nYou have successfully logged in!\n");
            } else {
                System.out.println("\nWrong card number or PIN!\n");
                isAuthenticated = false;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return isAuthenticated;
    }

    public void showBalance() {
        String selectBalance = """
                SELECT balance
                FROM card
                WHERE number = ?""";

        int balance;
        try (Connection connection = DriverManager.getConnection(bankingSystemDB.url);
             PreparedStatement preparedStatement = connection.prepareStatement(selectBalance)) {
            preparedStatement.setString(1, strUserAccountNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                balance = resultSet.getInt("balance");
                System.out.println("\nBalance: " + balance + "\n");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            exception.getMessage();
        }
    }

    public void doTransfer() {
        //1 step
        String senderBalance = "";
        String selectSenderBalance = """
                SELECT balance
                FROM card
                WHERE number = ?""";
        try (Connection connection = DriverManager.getConnection(bankingSystemDB.url);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSenderBalance)) {
            preparedStatement.setString(1, strUserAccountNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                senderBalance = resultSet.getString("balance");
            } else {
                //un mesaj de eroare
                return;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            exception.getMessage();
        }
        //2 step
        System.out.println("\nEnter card number: ");//numarul contului destinatar
        String recipientCardNumber = sc.nextLine();
        //3 step
        if (recipientCardNumber.equals(strUserAccountNumber)) {
            System.out.println("\nYou can't transfer money to the same account!\n");// 2nd message
            return;
        }
        //4 step
        if (!isValidLuhn(recipientCardNumber)) {
            System.out.println("\nProbably you made a mistake in the card number." +
                    "Please try again!\n");// 3rd message
            return;
        }
        //5 step
        String selectCardInDB = """
                SELECT number
                FROM card
                WHERE number = ?""";
        try (Connection connection = DriverManager.getConnection(bankingSystemDB.url);
             PreparedStatement preparedStatement = connection.prepareStatement(selectCardInDB)) {
            connection.setAutoCommit(false);
            preparedStatement.setString(1, recipientCardNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //6 step
                System.out.println("\nEnter how much money you want to transfer: ");
                int howMuchMoney = sc.nextInt();
                sc.nextLine();//clean the buffer

                String destinationBalance = """
                        UPDATE card
                        SET balance = balance + ?
                        WHERE number = ?""";

                String expeditorBalance = """
                        UPDATE card
                        SET balance = balance - ?
                        WHERE number = ?""";

                try (PreparedStatement prepStatDestinationAccount =
                             connection.prepareStatement(destinationBalance);
                     PreparedStatement prepStatExpeditorAccount =
                             connection.prepareStatement(expeditorBalance)) {

                    //7 step
                    if (howMuchMoney > Integer.parseInt(senderBalance)) {
                        System.out.println("\nNot enough money!\n");// 1st message
                        return;
                    } else {
                        //for destination balance
                        prepStatDestinationAccount.setInt(1, howMuchMoney);
                        prepStatDestinationAccount.setString(2, recipientCardNumber);
                        prepStatDestinationAccount.executeUpdate();

                        //for expeditor account
                        prepStatExpeditorAccount.setInt(1, howMuchMoney);
                        prepStatExpeditorAccount.setString(2, strUserAccountNumber);
                        prepStatExpeditorAccount.executeUpdate();

                        connection.commit();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("\nSuccess!\n"); // 5th message
//                }
            } else {
                System.out.println("\nSuch a card does not exist.\n");// 4th message
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            exception.getMessage();
        }
    }

    public void addFunds() {//add income
        System.out.println("\nEnter income: ");
        int amount = sc.nextInt();
        sc.nextLine();//clean the buffer
        new Main().setBalance(amount);
        String updateBalanceAccount = """
                UPDATE card
                SET balance = balance + ?
                WHERE number = ?
                AND pin = ?""";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(updateBalanceAccount)) {
            preparedStatement.setInt(1, getBalance());
            preparedStatement.setString(2, strUserAccountNumber);
            preparedStatement.setString(3, strUserCardPIN);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        System.out.println("\nIncome was added!\n");
    }

    public void closeAccount() {
        String delete = "DELETE FROM card WHERE number = ? AND pin = ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(delete)) {
            preparedStatement.setString(1, strUserAccountNumber);
            preparedStatement.setString(2, strUserCardPIN);
            preparedStatement.executeUpdate();
            System.out.println("The account has been closed!");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}

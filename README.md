# Simple Banking System 💳
This is a console-based banking system written in Java.

## 📘 Features:
- Create a new account with a card number and PIN.
  - *Validate card numbers using the **Luhn Algorithm**.*
- Log into an existing account.
  - 🔎 *Check current balance.*
  - ⤴️ *Add income to your account.*
  - ⤵️ *Transfer money to another account.*
    - Handle errors like:
      - Not enough balance.
      - Invalid card number or wrong PIN.  
      - Nonexistent destination account.
      - Transfer to the same account.
  - ❌ *Delete an account from the database.* 
  - 🚪 *Log out.*
- Exit.

## 📌 Getting Started:
### Requirements:
- Java Development Kit (JDK) 17.
- SQLite database.
- A terminal or command prompt.

## 📗 How to use:
### A short simulation of how the application works 💻:
```dif
1. Create an account
2. Log into account
0. Exit
2

Enter your card number: 
4000007430413127
Enter your PIN: 
9567

You have successfully logged in!

1. Balance
2. Add income
3. Do transfer
4. Close account
5. Log out
0. Exit
3

Enter card number: 
4000001189082837

Enter how much money you want to transfer: 
25

Success!

1. Balance
2. Add income
3. Do transfer
4. Close account
5. Log out
0. Exit
```
## Next Steps:
✅ Structuring the program in several classes.  
✅ Code optimization.  
✅ Creating a graphical interface.  

## 📖 License:
Licensed under the MIT License.
Created as part of **[Hyperskill](https://hyperskill.org)**.

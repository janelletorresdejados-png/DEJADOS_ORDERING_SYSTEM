package com.scc; // Updated to match your folder path

import com.scc.dao.DatabaseInitializer; // Updated import
import com.scc.dao.UserDao;             // Updated import
import com.scc.ui.Authentication;       // Updated import
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize structural DB checks
        DatabaseInitializer.initialize();

        // 2. Instantiate dependencies
        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();

        Authentication appEngine = new Authentication(userDao, sc);

        // 3. Kick off application sequence
        appEngine.start();

        // 4. Clean shutdown
        sc.close();
    }
}

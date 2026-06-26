package com.abonnements;

import com.abonnements.api.ApiServer;

public class Main {

    public static void main(String[] args) {
        int port = 8080;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation du port par defaut: 8080");
            }
        }

        System.out.println("Demarrage de Budget Tracker sur le port " + port + "...");
        ApiServer server = new ApiServer();
        server.start(port);
    }
}

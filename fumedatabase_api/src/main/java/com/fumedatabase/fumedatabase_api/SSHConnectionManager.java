package com.fumedatabase.fumedatabase_api;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHConnectionManager {
    private Session session;

    /**
     * Establishes an SSH connection to the remote server and sets up port forwarding.
     * @param user the username for the SSH connection
     * @param password the password for the SSH connection
     * @param rport the remote port to forward to the local machine
     * @param lport the local port to forward from the remote machine
     * @throws Exception
     */
    public void connect(String user, String password) throws Exception {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        session = jsch.getSession(user, "starbug.cs.rit.edu", 22);
        session.setPassword(password);
        session.setConfig(config);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.connect();
        System.out.println("Connected");
        session.setPortForwardingL(5432, "127.0.0.1", 5432);
        System.out.println("Port Forwarded");
    }

    /**
     * Returns the current SSH session.
     * @return the current Session object, or null if not connected
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Disconnected");
        }
    }
}

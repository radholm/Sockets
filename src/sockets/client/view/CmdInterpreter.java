/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.client.view;

import java.util.Scanner;
import sockets.client.controller.Controller;
import sockets.client.net.ServerResponse;

/**
 *
 * @author Fredrik
 */
public class CmdInterpreter implements Runnable {
    public static final String CONNECT = ".connect";
    public static final String QUIT = ".quit";
    public static final String NEWWORD = ".newword";
    public static final String HELP = ".help";
    private static final String PROMPT = "> ";
    private String[] strToken;
    private final Scanner console = new Scanner(System.in);
    private boolean inputCmds = false;
    private Controller contr;
    private final ThreadSafeOutput outMgr = new ThreadSafeOutput();

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (inputCmds) {
            return;
        }
        inputCmds = true;
        contr = new Controller();
        new Thread(this).start();
    }

    /**
     * Interprets and performs commands.
     */
    @Override
    public void run() {
        outMgr.println("Multithreaded hangman with sockets, .help for commands\n");
        while (inputCmds) {
            try {
                String command = (readNextLine());
                strToken = command.split("\\s+");
                switch (strToken[0]) {
                    case QUIT:
                        inputCmds = false;
                        contr.disconnect();
                        break;
                    case CONNECT:
                        contr.connect(strToken[1],
                                      Integer.parseInt(strToken[2]),
                                      new ConsoleOutput());
                        break;
                    case NEWWORD:
                        contr.sendMsg(command);
                        break;
                    case HELP:
                        outMgr.println("\nAvailable Commands:\n" 
                                        + QUIT + "\n" + CONNECT + " <ip adress> <port>" + "\n" 
                                        + NEWWORD + "\n" + HELP + "\n");
                    default:
                        contr.sendMsg(command);
                }
            } catch (Exception e) {
                outMgr.println("Operation failed. Try again.");
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    private class ConsoleOutput implements ServerResponse {
        @Override
        public void handleMsg(String msg) {
            outMgr.println((String) msg);
            outMgr.print(PROMPT);
        }
    }
}

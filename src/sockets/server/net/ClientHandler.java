/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.server.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import sockets.server.controller.Controller;

/**
 *
 * @author Fredrik
 */
class ClientHandler implements Runnable {
    private final Controller contr = new Controller();
    private final GameServer server;
    private final Socket clientSocket;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private boolean connected;
    private String secretWord;
    private int tries, score;
    private String wordToDisplay;
    final Set<String> correctChars;
    final Set<String> incorrectChars;
    
    ClientHandler(GameServer server, Socket clientSocket, String word, int tries) {
        this.incorrectChars = new HashSet<>();
        this.correctChars = new HashSet<>();
        this.server = server;
        this.clientSocket = clientSocket;
        this.secretWord = word;
        this.tries = tries;
        connected = true;
    }

    @Override
    public void run() {
        try {
            boolean autoFlush = true;
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            toClient = new PrintWriter(clientSocket.getOutputStream(), autoFlush);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        sendMsg(secretWord.replaceAll(".", "_ ") + " " + "lives: " + tries + " score: " + score);
        while (connected) {
            try {
                msgInterpreter(fromClient.readLine());
            } catch (IOException ioe) {
                disconnectClient();
            }
        }
    }
    
    void msgInterpreter(String msg) throws IOException {
        if(msg.equals(".newword")) {
            sendMsg("\nYou chose a new word\n");
            newRound();
        } else if(msg.contains(".")) {
            //command
        } else if(msg.matches(".*([ \\t]).*")) {
            sendMsg("You can't guess with a whitespace\n");
        } else {
            guessHandler(msg);
        }
    }
    
    void guessHandler(String guess) throws IOException {
        sendMsg("You guessed: " + guess);
        String res = contr.guessHandler(guess, secretWord);
        if (correctChars.contains(guess) || incorrectChars.contains(guess)) {
            sendMsg("You guessed that already!");
        } else if(res.equals("t")) {
            correctChars.add(guess);
        } else if(res.equals("f")) {
            incorrectChars.add(guess);
            --tries;
        } else if(res.equals("true")) {
            sendMsg("You were right!");            
            correctChars.add(guess);
        } else {
            sendMsg("You were wrong!");
            incorrectChars.add(guess);
            --tries;
        }
        wordToDisplay = contr.maskWord(correctChars, secretWord); 
        sendMsg(wordToDisplay + " " + "lives: " + tries + " Score: " + score);
        
        if (wordToDisplay.equals(secretWord)) {
            sendMsg("You won!\n");
            score++;
            newRound();
        } else if (incorrectChars.size() == secretWord.length()) {
            sendMsg("No tries left, you lost! Word was: " + secretWord + "\n");
            score--;
            newRound();
        }
    }
    
    void newRound() throws IOException {
        incorrectChars.clear();
        correctChars.clear();
        contr.createWords();
        secretWord = contr.getWord();
        tries = contr.getTries();
        sendMsg(secretWord.replaceAll(".", "_ ") + " " + "lives: " + tries + " score: " + score);
    }
    
    void sendMsg(String msg) {
        toClient.println(msg);
    }    
    
    private void disconnectClient() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
    }     
}

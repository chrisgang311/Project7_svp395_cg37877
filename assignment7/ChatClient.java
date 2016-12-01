/* Chat Client <ChatClient.java>
 * EE422C Project 7 submission by
 * <Samuel Patterson>
 * <svp395>
 * <16455>
 * <Christopher Gang>
 * <cg37877>
 * <16450>
 * Slip days used: <1>
 * Fall 2016
 */

package assignment7;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextArea;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatClient extends Application{
	private BufferedReader reader;
	private PrintWriter writer;
	public static String name;
	public Button enter;
	public TextArea nameText;
	
	private TextArea incoming;
	

	@Override
	public void start(Stage primaryStage) throws IOException, Exception {
		primaryStage.setTitle("Chat Room");
		setUpNetworking();
		
		
		incoming = new TextArea();
		incoming.setPrefHeight(400);
		
		incoming.setEditable(false);
		
		TextField outgoing = new TextField();
		outgoing.setEditable(true);
		Button send = new Button("Send");
		send.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writer.println(name + ": " + outgoing.getText());
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			}
		});
		
		send.setMaxWidth(100);
		
		TextField editGroup = new TextField();
		editGroup.setEditable(true);
		Button add = new Button("Add");
		add.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writer.println(ChatServer.GROUP_ADD + " " + name + " " + editGroup.getText());
				writer.flush();
				editGroup.setText("");
				editGroup.requestFocus();

			}
		});
		
		Button remove = new Button("Remove");
		remove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				writer.println(ChatServer.GROUP_REMOVE + " " + name + " " + editGroup.getText());
				writer.flush();
				editGroup.setText("");
				editGroup.requestFocus();

			}
		});
		
		

		HBox userInput = new HBox();
		userInput.setPadding(new Insets(10, 10, 10, 20));
		userInput.getChildren().addAll(outgoing, send);

		HBox addUsers = new HBox();
		addUsers.setPadding(new Insets(0, 10, 10, 20));
		addUsers.getChildren().addAll(editGroup, add, remove);
		
		
		VBox chatBox = new VBox();
		chatBox.setSpacing(10);
		chatBox.setPadding(new Insets(10, 20, 10, 20)); 
		chatBox.getChildren().addAll(incoming, userInput, addUsers);
		


		Scene scene = new Scene(chatBox, 500, 450);
		
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode == KeyCode.ENTER) {
					writer.println(name +  ": " + outgoing.getText());
					writer.flush();
					outgoing.setText("");
					outgoing.requestFocus();
				}
			}
        	
        });
		
		
		primaryStage.setScene(scene);
		primaryStage.show();
		


	}
	

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("192.168.0.20", 9028);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		
		writer = new PrintWriter(sock.getOutputStream());
		
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	public static void main(String[] args) {
		try {
			Application.launch(args);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if (name == null) {
						name = message;
						continue;
					}
					String temp = message;
					javafx.application.Platform.runLater( () -> incoming.appendText(temp + "\n") );
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}

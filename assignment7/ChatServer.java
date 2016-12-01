/* Chat Server <ChatServer.java>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ChatServer extends Observable
{
	private PrintWriter writer;
	private static Map<String, ClientObserver> clientObservers;
	private static Map<String, List<String>> groups;
	private static Map<String, Socket> map;
	private static ArrayList<String> names;
	public static final String GROUP_ADD = "GroupAdd";
	public static final String GROUP_REMOVE = "GroupRemove";

	public static void main(String[] args)
	{
		try
		{
			clientObservers = new HashMap<String, ClientObserver>();
			map = new HashMap<String, Socket>();
			groups = new HashMap<String, List<String>>();
			names = new ArrayList<String>();
			new ChatServer().setUpNetworking();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void addName(String name)
	{
		names.add(name);
	}

	private void setUpNetworking() throws IOException, Exception
	{
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(9028);
		while (true)
		{
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println(ip.getHostAddress());
			Socket clientSocket = serverSock.accept();
			Random rand = new Random();
			String name = "user" + rand.nextInt(1000);
			Iterator it = map.entrySet().iterator();
			List<String> allUsers = new ArrayList<String>();
			allUsers.add(name);
			initGroup(name, allUsers);
			writer = new PrintWriter(clientSocket.getOutputStream());
			writer.println(name);
			writer.flush();
			map.put(name, clientSocket);
			System.out.println(map.toString());
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			this.addObserver(writer);
			clientObservers.put(name, writer);
			setChanged();
			notifyObservers(name + " has logged in.");
			System.out.println("got a connection");
		}
	}

	public static void initGroup(String name, List<String> group)
	{
		groups.put(name, group);
	}

	class ClientHandler implements Runnable
	{
		private BufferedReader reader;

		public ClientHandler(Socket clientSocket)
		{
			Socket sock = clientSocket;
			try
			{
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public void run()
		{
			String message;
			try
			{
				while ((message = reader.readLine()) != null)
				{
					System.out.println("server read " + message);
					setChanged();
					String delims = "[ ]+";
					String[] tokens = message.split(delims);
					String username;
					if (tokens[0].equals(GROUP_ADD) || tokens[0].equals(GROUP_REMOVE))
					{
						Iterator it = map.entrySet().iterator();
						while (it.hasNext())
						{
							Map.Entry pair = (Map.Entry) it.next();
							if (pair.getKey().equals(tokens[2]))
							{
								Iterator tempIt = groups.entrySet().iterator();
								while (tempIt.hasNext())
								{
									Map.Entry tempPair = (Map.Entry) tempIt.next();
									if (tempPair.getKey().equals(tokens[1]))
									{
										List<String> tempGroup = (List<String>) tempPair.getValue();
										if (!tempGroup.contains(tokens[2]) && tokens[0].equals(GROUP_ADD))
										{
											tempGroup.add(tokens[2]);
											System.out.println(tokens[2] + " was added to " + tokens[1] + "'s group");
										} else if (tempGroup.contains(tokens[2]) && tokens[0].equals(GROUP_REMOVE))
										{
											tempGroup.remove(tokens[2]);
											System.out
													.println(tokens[2] + " was removed from " + tokens[1] + "'s group");

										}
									}
								}
							}
						}
					}

					String colonDelim = "[:]+";
					String[] chatTokens = message.split(colonDelim);
					Iterator it = groups.entrySet().iterator();
					ArrayList<String> updateGroup = new ArrayList<String>();
					while (it.hasNext())
					{
						Map.Entry pair = (Map.Entry) it.next();
						if (pair.getKey().equals(chatTokens[0]))
						{
							updateGroup = (ArrayList<String>) pair.getValue();
						}
					}

					deleteObservers();

					it = clientObservers.entrySet().iterator();
					while (it.hasNext())
					{
						Map.Entry pair = (Map.Entry) it.next();
						if (updateGroup.contains(pair.getKey()))
						{
							addObserver((Observer) pair.getValue());
						}
					}

					notifyObservers(message);

				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}

package vill.tictactoe;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class Server extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	DefaultListModel<String> logs = new DefaultListModel<String>();
	private JPanel contentPane;

	public static final int PORT = 8901;

	private Vector<Socket> conns = new Vector<Socket>();
	private ServerSocket listener = null;

	public Server() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));

		contentPane = new JPanel();
		contentPane.setLayout(null);
		getContentPane().add(contentPane);

		JLabel lblServerIP = new JLabel();
		lblServerIP.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblServerIP.setBounds(10, 36, 222, 41);
		contentPane.add(lblServerIP);

		JLabel lblGaCh = new JLabel(
				"Gõ địa chỉ IP bên dưới vào máy thằng bên cạnh:");
		lblGaCh.setLabelFor(lblServerIP);
		lblGaCh.setBounds(10, 11, 311, 14);
		contentPane.add(lblGaCh);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 88, 414, 160);

		JList<String> list = new JList<String>(logs);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);

		contentPane.add(scrollPane);

		try {
			String serverIp = InetAddress.getLocalHost().getHostAddress();
			lblServerIP.setText(serverIp);
		} catch (UnknownHostException e1) {
			lblServerIP.setText("Không thể lấy địa chỉ IP!");
			e1.printStackTrace();
		}

		setVisible(true);

		new Thread(this, "Server").start();
	}

	synchronized void printLogf(String format, Object... arg) {
		logs.addElement(String.format(format, arg));
	}

	public void run() {
		String name = Thread.currentThread().getName();
		try {

			new Thread(worker, "Dealer").start();

			printLogf("*** %s: Khởi tạo socket!", name);
			listener = new ServerSocket(PORT);
			printLogf("*** %s: Khởi tạo socket thành công.", name);

			while (true) {
				printLogf("*** %s: Đang chờ kết nối...", name);
				Socket client = listener.accept();

				printLogf("*** %s: Có Client mới kết nối: %s", name,
						client.getRemoteSocketAddress());

				synchronized (conns) {
					conns.add(client);
					conns.notifyAll();
				}
			}
		} catch (IOException e) {
			printLogf("*** %s: Có lỗi I/O xảy ra, đóng socket!. %s", name,
					e.getMessage());
			e.printStackTrace();
		}
	}

	// Worker: Kiểm tra danh sách các kết nối, nếu có đủ 2 kết nối thì
	// tạo thành ván chơi mới.
	private Runnable worker = new Runnable() {

		public void run() {

			String threadName = Thread.currentThread().getName();
			while (true) {
				int nPlayers = 0;
				do {
					nPlayers = conns.size();

					printLogf("*** %s: Có %d player(s) đang chờ.", threadName,
							nPlayers);

					if (nPlayers < 2) {
						synchronized (conns) {
							printLogf("*** %s: Chưa đủ người chơi!", threadName);
							try {
								conns.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} while (nPlayers < 2);

				synchronized (conns) {
					printLogf("*** %s: => Tạo ván chơi mới.", threadName);

					TicTacToeGame game = new TicTacToeGame();
					TicTacToeGame.Player playerX = game.new Player(
							conns.get(0), 'X');
					TicTacToeGame.Player playerO = game.new Player(
							conns.get(1), 'O');
					playerX.setOpponent(playerO);
					playerO.setOpponent(playerX);

					game.currentPlayer = playerX;

					playerX.start();
					playerO.start();

					conns.remove(0);
					conns.remove(0);
				}
			}

		}
	};

	/**
	 * A two-player game.
	 */
	class TicTacToeGame {

		/**
		 * A board has nine squares. Each square is either unowned or it is
		 * owned by a player. So we use a simple array of player references. If
		 * null, the corresponding square is unowned, otherwise the array cell
		 * stores a reference to the player that owns it.
		 */
		private Player[] board = { null, null, null, null, null, null, null,
				null, null };

		/**
		 * The current player.
		 */
		Player currentPlayer;

		/**
		 * Returns whether the current state of the board is such that one of
		 * the players is a winner.
		 */
		public boolean hasWinner() {
			return (board[0] != null && board[0] == board[1] && board[0] == board[2])
					|| (board[3] != null && board[3] == board[4] && board[3] == board[5])
					|| (board[6] != null && board[6] == board[7] && board[6] == board[8])
					|| (board[0] != null && board[0] == board[3] && board[0] == board[6])
					|| (board[1] != null && board[1] == board[4] && board[1] == board[7])
					|| (board[2] != null && board[2] == board[5] && board[2] == board[8])
					|| (board[0] != null && board[0] == board[4] && board[0] == board[8])
					|| (board[2] != null && board[2] == board[4] && board[2] == board[6]);
		}

		/**
		 * Returns whether there are no more empty squares.
		 */
		public boolean boardFilledUp() {
			for (int i = 0; i < board.length; i++) {
				if (board[i] == null) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Called by the player threads when a player tries to make a move. This
		 * method checks to see if the move is legal: that is, the player
		 * requesting the move must be the current player and the square in
		 * which she is trying to move must not already be occupied. If the move
		 * is legal the game state is updated (the square is set and the next
		 * player becomes current) and the other player is notified of the move
		 * so it can update its client.
		 */
		public synchronized boolean legalMove(int location, Player player) {
			if (player == currentPlayer && board[location] == null) {
				board[location] = currentPlayer;
				currentPlayer = currentPlayer.opponent;
				currentPlayer.otherPlayerMoved(location);
				return true;
			}
			return false;
		}

		/**
		 * The class for the helper threads in this multithreaded server
		 * application. A Player is identified by a character mark which is
		 * either 'X' or 'O'. For communication with the client the player has a
		 * socket with its input and output streams. Since only text is being
		 * communicated we use a reader and a writer.
		 */
		class Player extends Thread {
			char mark;
			Player opponent;
			Socket socket;
			BufferedReader i;
			PrintWriter o;

			/**
			 * Constructs a handler thread for a given socket and mark
			 * initializes the stream fields, displays the first two welcoming
			 * messages.
			 */
			public Player(Socket socket, char mark) {
				this.socket = socket;
				this.mark = mark;
				try {
					i = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					o = new PrintWriter(socket.getOutputStream(), true);
					o.println("WELCOME " + mark);
					o.println("MESSAGE Waiting for opponent to connect");
				} catch (IOException e) {
					System.out.println("Player died: " + e);
				}
			}

			/**
			 * Accepts notification of who the opponent is.
			 */
			public void setOpponent(Player opponent) {
				this.opponent = opponent;
			}

			/**
			 * Handles the otherPlayerMoved message.
			 */
			public void otherPlayerMoved(int location) {
				o.println("OPPONENT_MOVED " + location);
				o.println(hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
			}

			/**
			 * The run method of this thread.
			 */
			public void run() {
				try {
					// The thread is only started after everyone connects.
					o.println("MESSAGE Tất cả người chơi đã kết nối");

					// Tell the first player that it is her turn.
					if (mark == 'X') {
						o.println("MESSAGE Bạn đi trước");
					}

					// Repeatedly get commands from the client and process them.
					String command = null;
					while (true) {
						try {
							command = i.readLine();
							if (command == null) {
								break;
							}
						} catch (IOException e) {
							o.println("VICTORY");
							System.out.println("Player died: " + e);
							return;
						}
						if (command.startsWith("MOVE")) {
							int location = Integer.parseInt(command
									.substring(5));

							if (legalMove(location, this)) {
								o.println("VALID_MOVE");
								o.println(hasWinner() ? "VICTORY"
										: boardFilledUp() ? "TIE" : "");
							} else {
								o.println("MESSAGE ?");
							}
						} else if (command.startsWith("QUIT")) {
							return;
						}
					}

				} finally {
					try {
						socket.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
}

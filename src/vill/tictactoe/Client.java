package vill.tictactoe;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * A client for the TicTacToe game, modified and extended from the class
 * presented in Deitel and Deitel "Java How to Program" book. I made a bunch of
 * enhancements and rewrote large sections of the code. In particular I created
 * the TTTP (Tic Tac Toe Protocol) which is entirely text based. Here are the
 * strings that are sent:
 * 
 * Client -> Server Server -> Client ---------------- ---------------- MOVE <n>
 * (0 <= n <= 8) WELCOME <char> (char in {X, O}) QUIT VALID_MOVE
 * OTHER_PLAYER_MOVED <n> VICTORY DEFEAT TIE MESSAGE <text>
 * 
 */
public class Client implements Runnable {

	private JFrame frame = null;
	
	JLabel messageLabel ;
	private String icon;
	private String opponentIcon;

	private Square[] board = new Square[9];
	private Square currentSquare;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	/**
	 * Constructs the client by connecting to a server, laying out the GUI and
	 * registering GUI listeners.
	 */
	public Client() {

		frame = new JFrame("Tic Tac Toe");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(304, 351);
		frame.setVisible(true);
		frame.setResizable(false);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);

		JPanel boardPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, boardPanel, 0, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, boardPanel, -26, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, boardPanel, 0,
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, boardPanel, 0,
				SpringLayout.EAST, frame.getContentPane());
		boardPanel.setBackground(Color.black);
		boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
		for (int i = 0; i < board.length; i++) {
			final int j = i;
			board[i] = new Square();
			board[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					currentSquare = board[j];
					out.println("MOVE " + j);
				}
			});
			boardPanel.add(board[i]);
		}
		frame.getContentPane().add(boardPanel);
		
		messageLabel = new JLabel(".....");
		springLayout.putConstraint(SpringLayout.NORTH, messageLabel, 0, SpringLayout.SOUTH, boardPanel);
		springLayout.putConstraint(SpringLayout.WEST, messageLabel, 5, SpringLayout.WEST, boardPanel);
		springLayout.putConstraint(SpringLayout.SOUTH, messageLabel, 0, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, messageLabel, 0, SpringLayout.EAST, boardPanel);
		frame.getContentPane().add(messageLabel);

		while (socket == null) {
			String s = (String) JOptionPane.showInputDialog(frame,
					"Gõ vào địa chỉ máy chủ TicTacToe:", "Tạo kết nối",
					JOptionPane.QUESTION_MESSAGE, null, null, "localhost");
			if (s == null) {
				frame.dispose();
				break;
			} else {
				try {
					socket = new Socket(s, Server.PORT);

					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (UnknownHostException e) {
					e.printStackTrace();

					JOptionPane.showMessageDialog(frame, String.format(
							"Không thể kết nối đến máy chủ \"%s\"\n"
									+ "Vui lòng kiểm tra lại địa chỉ.\n", s),
							"Thử lại", JOptionPane.ERROR_MESSAGE);
					socket = null;
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane
							.showMessageDialog(
									frame,
									String.format(
											"Không thể kết nối đến máy chủ \"%s\"\n"
													+ "Đảm bảo máy chủ Tic Tac Toe đã được khởi động trước.",
											s), "Thử lại",
									JOptionPane.ERROR_MESSAGE);
					socket = null;
				}
			}
		}

		new Thread(this, "Client").start();
	}

	/**
	 * The main thread of the client will listen for messages from the server.
	 * The first message will be a "WELCOME" message in which we receive our
	 * mark. Then we go into a loop listening for "VALID_MOVE",
	 * "OPPONENT_MOVED", "VICTORY", "DEFEAT", "TIE", "OPPONENT_QUIT or "MESSAGE"
	 * messages, and handling each message appropriately. The "VICTORY",
	 * "DEFEAT" and "TIE" ask the user whether or not to play another game. If
	 * the answer is no, the loop is exited and the server is sent a "QUIT"
	 * message. If an OPPONENT_QUIT message is recevied then the loop will exit
	 * and the server will be sent a "QUIT" message also.
	 */
	public void run() {
		String response;
		try {
			response = in.readLine();
			if (response.startsWith("WELCOME")) {
				char mark = response.charAt(8);
				icon = mark == 'X' ? "X" : "O";
				opponentIcon = mark == 'X' ? "O" : "X";
				frame.setTitle("Tic Tac Toe - Player " + mark);
			}
			while (true) {
				response = in.readLine();
				if (response.startsWith("VALID_MOVE")) {
					messageLabel.setText("Chờ đối thủ...");
					currentSquare.setText(icon);
					currentSquare.repaint();
				} else if (response.startsWith("OPPONENT_MOVED")) {
					int loc = Integer.parseInt(response.substring(15));
					board[loc].setText(opponentIcon);
					board[loc].repaint();
					messageLabel.setText("Đến lượt bạn!");
				} else if (response.startsWith("VICTORY")) {
					messageLabel.setText("Thắng rồi !!!");
					break;
				} else if (response.startsWith("DEFEAT")) {
					messageLabel.setText("Thua...");
					break;
				} else if (response.startsWith("TIE")) {
					messageLabel.setText("Hòa. :)");
					break;
				} else if (response.startsWith("MESSAGE")) {
					messageLabel.setText(response.substring(8));
				}
			}
			out.println("QUIT");
		} catch (IOException e) {
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean wantsToPlayAgain() {
		int response = JOptionPane.showConfirmDialog(frame,
				"Want to play again?", "Tic Tac Toe is Fun Fun Fun",
				JOptionPane.YES_NO_OPTION);
		frame.dispose();
		return response == JOptionPane.YES_OPTION;
	}

	/**
	 * Graphical square in the client window. Each square is a white panel
	 * containing. A client calls setIcon() to fill it with an Icon, presumably
	 * an X or O.
	 */
	static class Square extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8475424146955045105L;
		JLabel label = new JLabel();

		public Square() {
			setBackground(Color.white);
			add(label);
		}

		public void setText(String text) {
			label.setText(text);
		}
	}
}
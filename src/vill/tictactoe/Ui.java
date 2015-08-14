package vill.tictactoe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpringLayout;

public class Ui {

	JFrame mainFrame = null;

	Ui() {
		mainFrame = new JFrame("VILL - TicTacToe");

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(380, 148);
		mainFrame.setVisible(true);
		mainFrame.setResizable(false);
		SpringLayout springLayout = new SpringLayout();
		mainFrame.getContentPane().setLayout(springLayout);

		JButton btnStartServer = new JButton("Chạy máy chủ");
		springLayout.putConstraint(SpringLayout.NORTH, btnStartServer, 10,
				SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, btnStartServer, 10,
				SpringLayout.WEST, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnStartServer, 56,
				SpringLayout.NORTH, mainFrame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnStartServer, -11,
				SpringLayout.EAST, mainFrame.getContentPane());
		mainFrame.getContentPane().add(btnStartServer);

		JButton btnStartClient = new JButton("Chạy máy khách");
		springLayout.putConstraint(SpringLayout.NORTH, btnStartClient, 6,
				SpringLayout.SOUTH, btnStartServer);
		springLayout.putConstraint(SpringLayout.WEST, btnStartClient, 0,
				SpringLayout.WEST, btnStartServer);
		springLayout.putConstraint(SpringLayout.SOUTH, btnStartClient, 52,
				SpringLayout.SOUTH, btnStartServer);
		springLayout.putConstraint(SpringLayout.EAST, btnStartClient, -11,
				SpringLayout.EAST, mainFrame.getContentPane());
		mainFrame.getContentPane().add(btnStartClient);

		btnStartServer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Server();
			}
		});

		btnStartClient.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Client();
			}
		});

	}

	public static void main(String[] args) {
		new Ui();
	}
}

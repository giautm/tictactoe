package vill.tictactoe;

import java.util.Scanner;

public class Server {

	public static void main(String[] args) {
		System.out.println("Nhap vao loai nha cua ban");
		System.out.println("1. Nha pho");
		System.out.println("2. Biet thu");
		System.out.println("3. Chung cu");
		
		Scanner scnr = new Scanner(System.in);
		int lua_chon = scnr.nextInt();
		
		String loai_nha = "Nhà lá";
		if (lua_chon == 1) {
			loai_nha = "Nhà phố";
		} else if (lua_chon == 2) {
			loai_nha = "Biệt thự";
		} else if (lua_chon == 3) {
			loai_nha = "Chung cư";
		}
		
		System.out.println("Nhà của bạn: " + loai_nha);
	}

}

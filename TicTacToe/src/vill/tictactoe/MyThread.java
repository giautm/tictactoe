package vill.tictactoe;

public class MyThread implements Runnable {
	private String youName;
	
	public MyThread(String youName) {
		super();
		this.youName = youName;
	}

	public void run() {
		System.out.println("Xin chào " + youName);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		System.out.println("Tạm biệt " + youName);
	}

}

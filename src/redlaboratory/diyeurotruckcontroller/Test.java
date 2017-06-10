package redlaboratory.diyeurotruckcontroller;

import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;

public class Test {
	
	private ConsoleReader console;
	private CursorBuffer stashed;
	
	private boolean runThread;

	public static void main(String[] args) {
		Test test = new Test();
		test.init();
		test.run();
	}
	
	public void init() {
		try {
			console = new ConsoleReader();
			runThread = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		new Thread(() -> {
			while (runThread) {
				try {
					print("something");
					Thread.sleep(1000);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		while (true) {
			try {
				String scan = console.readLine("prompt> ");
				if (scan.equals("exit")) {
					runThread = false;
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void print(String str) throws IOException {
		stashLine();
		console.println(str);
		console.flush();
		unstashLine();
	}
	
	/**
	 * before you want to output new data
	 */
	private void stashLine() {
	    this.stashed = this.console.getCursorBuffer().copy();
	    try {
	        this.console.getOutput().write("\u001b[1G\u001b[K");
	        this.console.flush();
	    } catch (IOException e) {
	        // ignore
	    }
	}

	/**
	 * after output
	 */
	private void unstashLine() {
	    try {
	        this.console.resetPromptLine(this.console.getPrompt(),
	          this.stashed.toString(), this.stashed.cursor);
	    } catch (IOException e) {
	        // ignore
	    }
	}
	
}

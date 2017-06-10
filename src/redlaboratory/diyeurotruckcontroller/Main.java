package redlaboratory.diyeurotruckcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import com.google.gson.Gson;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import redlaboratory.jvjoyinterface.VJoy;
import redlaboratory.jvjoyinterface.VjdStat;

public class Main {
	
	public static void main(String[] args) {
		try {
			new Main().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int rID;
	private VJoy vJoy;
	
	private static final int TIME_OUT = 1000;
	private static final int DATA_RATE = 9600;
	private SerialPort serialPort;
	private InputStream input;
	@SuppressWarnings("unused")
	private OutputStream output;
	private StringBuilder strBuf;
	private Gson gson;
	
	private ConsoleReader console;
	private CursorBuffer stashed;
	private String prompt = "prompt> ";
	
	public Main() {
		try {
			console = new ConsoleReader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() throws IOException {
		if (initVJoy()) {
			info("Succeed to initialize vJoy.");
		} else {
			info("Failed to initialize vJoy.");
			
			return;
		}
		
		println("Please enter port name.", false);
		String portName = console.readLine("> ");
		if (initRXTX(new String[] {portName})) {
			info("Succeed to initialize RXTX.");
		} else {
			info("Failed to initialize RXTX.");
			
			return;
		}
		
		println("", false);
		
		while (true) {
			String rawCommand = console.readLine(prompt);
			if (rawCommand.equals("exit")) {
				closeRXTX();
				break;
			}
		}
	}
	
	private boolean initVJoy() {
		rID = 1;
		vJoy = new VJoy();
		
		if (!vJoy.vJoyEnabled()) return false;
		
		VjdStat status = vJoy.getVJDStatus(rID);
		if (status == VjdStat.VJD_STAT_FREE && vJoy.acquireVJD(rID)) {
			info("VJoy is acquired: {deviceNumber: " + rID + "}");
		} else {
			info("Acquiring VJoy is failed: {deviceNumber: " + rID + "}");
			
			return false;
		}
		
		return true;
	}
	
	private boolean initRXTX(String[] portNames) {
		strBuf = new StringBuilder();
		gson = new Gson();
		
		CommPortIdentifier portID = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
		
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier curPortID = (CommPortIdentifier) portEnum.nextElement();
			
			for (String portName : portNames) {
				if (curPortID.getName().equalsIgnoreCase(portName)) {
					portID = curPortID;
					
					break;
				}
			}
		}
		
		if (portID == null) {
			info("Could not find COM port.");
			
			return false;
		}
		
		info("Found COM port: {portName: " + portID.getName() + "}");
		
		try {
			serialPort = (SerialPort) portID.open("", TIME_OUT);
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			serialPort.addEventListener(new SerialEventListener());
			serialPort.notifyOnDataAvailable(true);
		} catch (PortInUseException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (TooManyListenersException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void closeRXTX() {
		if (serialPort != null) {
			info("Closing rxtx.");
			
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	private void onMessage(Message message) {
		int[] data = message.data;
		
//		long steer = (long) (message.data[0] / 1024.0 * VJoy.AXIS_MAX_VALUE) / 3 + VJoy.AXIS_MID_VALUE - VJoy.AXIS_MAX_VALUE / 3 / 2;
		long steer = (long) (data[0] / 1024.0 * VJoy.AXIS_MAX_VALUE);
		long acceleration = data[3] == 1 ? VJoy.AXIS_MAX_VALUE : VJoy.AXIS_MIN_VALUE;
		long braking = data[4] == 1 ? VJoy.AXIS_MAX_VALUE : VJoy.AXIS_MIN_VALUE;
		
		vJoy.setAxis(steer, rID, VJoy.HID_USAGE_RX);
		vJoy.setAxis(acceleration, rID, VJoy.HID_USAGE_RY);
		vJoy.setAxis(braking, rID, VJoy.HID_USAGE_RZ);
		vJoy.setBtn(data[5] == 1, rID, 3);
		vJoy.setBtn(data[6] == 1, rID, 4);
		vJoy.setBtn(data[7] == 1, rID, 5);
		vJoy.setBtn(data[8] == 1, rID, 6);
	}
	
	public void info(String str) {
		println("[info] ".concat(str), false);
	}
	
	public void println(String str, boolean overwrite) {
		try {
			this.stashed = this.console.getCursorBuffer().copy();
			this.console.getOutput().write("\u001b[1G\u001b[K");
			this.console.flush();
	        
			if (overwrite) {
				console.getOutput().write("\u001b[1A\u001b[K");
				console.flush();
			}
			
			console.println(str);
			console.flush();
			
			this.console.resetPromptLine(this.console.getPrompt(), this.stashed.toString(), this.stashed.cursor);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	public void print(String str) {
		try {
			this.stashed = this.console.getCursorBuffer().copy();
			this.console.getOutput().write("\u001b[1G\u001b[K");
			this.console.flush();
	        
			console.print(str);
			console.flush();
			
			this.console.resetPromptLine(this.console.getPrompt(),
			this.stashed.toString(), this.stashed.cursor);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	private class Message {
		public int[] data;
	}
	
	private class SerialEventListener implements SerialPortEventListener {
		
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					int length = input.available();
					byte[] buffer = new byte[length];
					input.read(buffer, 0, length);
					
					strBuf.append(new String(buffer, 0, length));
					
					int index = strBuf.indexOf("\n");
					if (index != -1) {
						String messageStr = strBuf.substring(0, index + 1);
						strBuf.delete(0, index + 1);
						
						Message message = gson.fromJson(messageStr, Message.class);
						onMessage(message);
						
						println(messageStr, true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}

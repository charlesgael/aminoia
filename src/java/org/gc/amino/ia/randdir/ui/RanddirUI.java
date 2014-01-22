package org.gc.amino.ia.randdir.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gc.amino.awt.AWTLauncher;
import org.gc.amino.ia.randdir.RanddirIA;
import org.gc.amino.ia.randdir.serv.RequestListenerThread;

import com.sun.corba.se.pept.transport.ListenerThread;

public class RanddirUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private static int port;

	private JLabel statusText;
	private Component nbIARunning;

	private RequestListenerThread listenerThread;

	private CapturePane console_out;

	private static RanddirUI ui;

	public static RanddirUI getUI() {
		if (ui == null) {
			try {
				ui = new RanddirUI(port);
			} catch (IOException e) {}
		}
		return ui;
	}

	private RanddirUI(int port) throws IOException {
		super("Randdir IA");

		int w = 400;

		setSize(w, 550);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		setLocation((int) (width-w), 0);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		listenerThread = new RequestListenerThread(this, port, RanddirIA.class);
		listenerThread.setDaemon(false);

		setupUI();

        PrintStream ps = System.out;
		System.setOut(new PrintStream(new StreamCapturer(">", console_out, ps)));

		listenerThread.start();
	}

	public void setStatus(String status) {
		statusText.setText(status);
	}

	private void setupUI() {
		setLayout(new BorderLayout());

		{
			JPanel status = new JPanel();
			status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
			status.setLayout(new BorderLayout());

			{
				JPanel leading = new JPanel();
				leading.setLayout(new FlowLayout(FlowLayout.LEFT));

				{
					statusText = new JLabel();
					leading.add(statusText);
				}

				status.add(leading, BorderLayout.WEST);
			}

			{
				JPanel trailing = new JPanel();
				trailing.setLayout(new FlowLayout(FlowLayout.LEFT));

				{
					JButton btn = new JButton("Restart");
					btn.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
					btn.setHorizontalAlignment(JButton.LEADING); // optional
					btn.setBorderPainted(false);
					btn.setContentAreaFilled(false);
					btn.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent evt) {
							listenerThread.reset();
							console_out.output.setText("");
							System.out.println("Reseted IA");
						}
					});
					trailing.add(btn);
				}

				status.add(trailing, BorderLayout.EAST);
			}

			add(status, BorderLayout.PAGE_START);
		}

		{
			console_out = new CapturePane();

			add(console_out, BorderLayout.CENTER);
		}

		setAlwaysOnTop(true);
	}

	public class CapturePane extends JPanel implements Consumer {

		private JTextArea output;

		public CapturePane() {
			setLayout(new BorderLayout());
			output = new JTextArea();
			add(new JScrollPane(output));
		}

		@Override
		public void appendText(final String text) {
			if (EventQueue.isDispatchThread()) {
				output.append(text);
				output.setCaretPosition(output.getText().length());
			} else {

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						appendText(text);
					}
				});

			}
		}        
	}

	public interface Consumer {        
		public void appendText(String text);        
	}

	public class StreamCapturer extends OutputStream {

		private StringBuilder buffer;
		private String prefix;
		private Consumer consumer;
		private PrintStream old;

		public StreamCapturer(String prefix, Consumer consumer, PrintStream old) {
			this.prefix = prefix;
			buffer = new StringBuilder(128);
			buffer.append(prefix).append(" ");
			this.old = old;
			this.consumer = consumer;
		}

		@Override
		public void write(int b) throws IOException {
			char c = (char) b;
			String value = Character.toString(c);
			buffer.append(value);
			if (value.equals("\n")) {
				consumer.appendText(buffer.toString());
				buffer.delete(0, buffer.length());
				buffer.append(prefix).append(" ");
			}
			old.print(c);
		}        
	}   

	public static void main(String... args) throws IOException {
		port = 1234;
		// you may optionally specify on commandline the port to use
		if ( args.length == 1 ) {
			port = Integer.parseInt( args[0] );
		}

		RanddirUI.getUI().setVisible(true);
	}
}

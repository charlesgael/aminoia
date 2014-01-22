package org.gc.amino.ia.randdir.serv;

import java.io.IOException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.gc.amino.ia.randdir.serv.RequestListenerThread.MyHttpRequestHandler;
import org.gc.amino.ia.randdir.ui.RanddirUI;

public class InstanceIA extends Thread {

	private final HttpService httpservice;
	private final HttpServerConnection conn;
	private MyHttpRequestHandler httpRequestHandler;
	private RanddirUI ui;

	public InstanceIA( final HttpService httpservice,
			final HttpServerConnection conn,
			MyHttpRequestHandler httpRequestaHandler, RanddirUI ui ) { 
		super();
		this.httpservice = httpservice;
		this.conn = conn;
		this.httpRequestHandler = httpRequestaHandler;
		this.ui = ui;
	}

	@Override
	public void run() {
		System.out.println("New connection thread");
		HttpContext context = new BasicHttpContext(null);
		try {
			while (!Thread.interrupted() && this.conn.isOpen()) {
				this.httpservice.handleRequest(this.conn, context);
			}
		} catch (ConnectionClosedException ex) {
			httpRequestHandler.reset();
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex);
		} catch (HttpException ex) {
			System.err.println("Unrecoverable HTTP protocol violation: " + ex);
		} finally {
			try {
				this.conn.shutdown();
			} catch (IOException ignore) {}
		}
	}
}

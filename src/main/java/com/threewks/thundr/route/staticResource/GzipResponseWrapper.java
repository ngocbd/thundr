package com.threewks.thundr.route.staticResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GzipResponseWrapper extends HttpServletResponseWrapper {
	protected HttpServletResponse origResponse;
	protected ServletOutputStream stream;
	protected PrintWriter writer;
	protected int threshold;
	protected String contentType;

	public GzipResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		origResponse.setContentType(contentType);
	}

	public void setCompressionThreshold(int threshold) {
		this.threshold = threshold;
	}

	public ServletOutputStream createOutputStream() throws IOException {
		GzipResponseStream gzstream = new GzipResponseStream(origResponse);
		gzstream.setBuffer(threshold);
		return gzstream;
	}

	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					stream.close();
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		if (stream != null) {
			stream.flush();
		}
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");
		}
		if (stream == null) {
			stream = createOutputStream();
		}
		return (stream);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return writer;
		}

		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called for this response");
		}

		stream = createOutputStream();

		String charEnc = origResponse.getCharacterEncoding();
		if (charEnc != null) {
			writer = new PrintWriter(new OutputStreamWriter(stream, charEnc));
		} else {
			writer = new PrintWriter(stream);
		}
		return (writer);
	}

	@Override
	public void setContentLength(int length) {
	}

}
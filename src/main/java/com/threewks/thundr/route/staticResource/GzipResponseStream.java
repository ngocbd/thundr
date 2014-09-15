package com.threewks.thundr.route.staticResource;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class GzipResponseStream extends ServletOutputStream {
	public GzipResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
	}

	/**
	 * The threshold number which decides to compress or not.
	 */
	protected int compressionThreshold;
	protected byte[] buffer;
	protected int bufferCount;
	protected GZIPOutputStream gzipstream;
	protected boolean closed;

	/**
	 * The content length past which we will not write, or -1 if there is no
	 * defined content length.
	 */
	protected int length = -1;
	protected HttpServletResponse response;
	protected ServletOutputStream output;

	/**
	 * Sets the compressionThreshold number and create buffer for this size.
	 */
	protected void setBuffer(int threshold) {
		compressionThreshold = threshold;
		buffer = new byte[compressionThreshold];
	}

	@Override
	public void close() throws IOException {
		if (closed == true) {
			return;
		}
		if (gzipstream != null) {
			flushToGZip();
			gzipstream.close();
			gzipstream = null;
		} else {
			if (bufferCount > 0) {
				output.write(buffer, 0, bufferCount);
				bufferCount = 0;
			}
		}
		output.close();
		closed = true;
	}

	@Override
	public void flush() throws IOException {

		if (closed) {
			return;
		}
		if (gzipstream != null) {
			gzipstream.flush();
		}
	}

	public void flushToGZip() throws IOException {
		if (bufferCount > 0) {
			writeToGZip(buffer, 0, bufferCount);
			bufferCount = 0;
		}
	}

	@Override
	public void write(int b) throws IOException {

		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		if (bufferCount >= buffer.length) {
			flushToGZip();
		}
		buffer[bufferCount++] = (byte) b;
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}

		if (len == 0) {
			return;
		}

		// Can we write into buffer ?
		if (len <= (buffer.length - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			return;
		}

		// There is not enough space in buffer. Flush it ...
		flushToGZip();

		// ... and try again. Note, that bufferCount = 0 here !
		if (len <= (buffer.length - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			return;
		}

		// write direct to gzip
		writeToGZip(b, off, len);
	}

	public void writeToGZip(byte b[], int off, int len) throws IOException {

		if (gzipstream == null) {
			gzipstream = new GZIPOutputStream(output);
			response.addHeader("Content-Encoding", "gzip");
		}
		gzipstream.write(b, off, len);

	}

	public boolean closed() {
		return (this.closed);
	}

}

/*
 * Copyright (c) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.callme.platform.util;

import android.database.Cursor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	private IOUtils() {
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Throwable e) {
			}
		}
	}

	public static void closeQuietly(Cursor cursor) {
		if (cursor != null) {
			try {
				cursor.close();
			} catch (Throwable e) {
			}
		}
	}

	/** {@value} */
	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB
	/** {@value} */
	public static final int DEFAULT_IMAGE_TOTAL_SIZE = 500 * 1024; // 500 Kb
	/** {@value} */
	public static final int CONTINUE_LOADING_PERCENTAGE = 75;

	private static final int STRING_DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * Copies stream, fires progress events by listener, can be interrupted by
	 * listener. Uses buffer size = {@value #DEFAULT_BUFFER_SIZE} bytes.
	 *
	 * @param is
	 *            Input stream
	 * @param os
	 *            Output stream
	 * @param listener
	 *            null-ok; Listener of copying progress and controller of
	 *            copying interrupting
	 * @return <b>true</b> - if stream copied successfully; <b>false</b> - if
	 *         copying was interrupted by listener
	 * @throws IOException
	 */
	public static boolean copyStream(InputStream is, OutputStream os,
			CopyListener listener) throws IOException {
		return copyStream(is, os, listener, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies stream, fires progress events by listener, can be interrupted by
	 * listener.
	 *
	 * @param is
	 *            Input stream
	 * @param os
	 *            Output stream
	 * @param listener
	 *            null-ok; Listener of copying progress and controller of
	 *            copying interrupting
	 * @param bufferSize
	 *            Buffer size for copying, also represents a step for firing
	 *            progress listener callback, i.e. progress event will be fired
	 *            after every copied <b>bufferSize</b> bytes
	 * @return <b>true</b> - if stream copied successfully; <b>false</b> - if
	 *         copying was interrupted by listener
	 * @throws IOException
	 */
	public static boolean copyStream(InputStream is, OutputStream os,
			CopyListener listener, int bufferSize) throws IOException {
		int current = 0;
		int total = is.available();
		if (total <= 0) {
			total = DEFAULT_IMAGE_TOTAL_SIZE;
		}

		final byte[] bytes = new byte[bufferSize];
		int count;
		if (shouldStopLoading(listener, current, total))
			return false;
		while ((count = is.read(bytes, 0, bufferSize)) != -1) {
			os.write(bytes, 0, count);
			current += count;
			if (shouldStopLoading(listener, current, total))
				return false;
		}
		os.flush();
		return true;
	}

	private static boolean shouldStopLoading(CopyListener listener,
			int current, int total) {
		if (listener != null) {
			boolean shouldContinue = listener.onBytesCopied(current, total);
			if (!shouldContinue) {
				if (100 * current / total < CONTINUE_LOADING_PERCENTAGE) {
					return true; // if loaded more than 75% then continue
									// loading anyway
				}
			}
		}
		return false;
	}

	/**
	 * Reads all data from stream and close it silently
	 *
	 * @param is
	 *            Input stream
	 */
	public static void readAndCloseStream(InputStream is) {
		final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
		try {
			while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1)
				;
		} catch (IOException ignored) {
		} finally {
			closeSilently(is);
		}
	}

	public static void closeSilently(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ignored) {
			}
		}
	}

	/** Listener and controller for copy process */
	public interface CopyListener {
		/**
		 * @param current
		 *            Loaded bytes
		 * @param total
		 *            Total bytes for loading
		 * @return <b>true</b> - if copying should be continued; <b>false</b> -
		 *         if copying should be interrupted
		 */
		boolean onBytesCopied(int current, int total);
	}

	public static ByteArrayInputStream toByteArrayInputStream(InputStream is)
			throws IOException, OutOfMemoryError {
		int len = is.available();
		byte[] data = new byte[len];
		is.read(data);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return bais;
	}

	public static ByteArrayOutputStream toByteArrayOutputStream(
			InputStream input) throws IOException, OutOfMemoryError {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output;
	}

	public static byte[] toByteArray(InputStream input) throws IOException,
			OutOfMemoryError {
		return toByteArrayOutputStream(input).toByteArray();
	}

	public static int copy(InputStream input, OutputStream output)
			throws IOException, OutOfMemoryError {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(InputStream input, OutputStream output)
			throws IOException, OutOfMemoryError {
		if (input == null) {
			return -1;
		}

		byte[] buffer = new byte[STRING_DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}

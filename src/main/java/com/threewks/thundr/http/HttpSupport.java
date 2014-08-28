/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.http;

public class HttpSupport {

	/**
	 * Http Methods (i.e. GET, POST etc).
	 * 
	 * Includes all methods defined <a href="http://tools.ietf.org/html/rfc2616#section-5.1.1">here</a> as well as conventional methods, such as PATCH.
	 */
	public static class Methods {
		public static final String Options = "OPTIONS";
		public static final String Get = "GET";
		public static final String Head = "HEAD";
		public static final String Post = "POST";
		public static final String Put = "PUT";
		public static final String Delete = "DELETE";
		public static final String Trace = "TRACE";
		public static final String Connect = "CONNECT";
		public static final String Patch = "PATCH";

		/**
		 * @param method
		 * @return true if the given method is GET, case-insensitive
		 */
		public static boolean isGet(String method) {
			return Get.equalsIgnoreCase(method);
		}

		/**
		 * 
		 * @param method
		 * @return true if the given method is PUT, case-insensitive
		 */
		public static boolean isPut(String method) {
			return Put.equalsIgnoreCase(method);
		}

		/**
		 * 
		 * @param method
		 * @return true if the given method is POST, case-insensitive
		 */
		public static boolean isPost(String method) {
			return Post.equalsIgnoreCase(method);
		}

		/**
		 * 
		 * @param method
		 * @return true if the given method is DELETE, case-insensitive
		 */
		public static boolean isDelete(String method) {
			return Delete.equalsIgnoreCase(method);
		}

		/**
		 * @param method
		 * @return true if the given method is PATCH, case-insensitive
		 */
		public static boolean isPatch(String method) {
			return Patch.equalsIgnoreCase(method);
		}
	}

	
}

/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
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

import java.util.LinkedHashMap;
import java.util.Map;

public enum StatusCode {
	Continue(100, "Continue"),
	SwitchingProtocols(101, "Switching Protocols"),
	Processing(102, "Processing"),
	OK(200, "OK"),
	Created(201, "Created"),
	Accepted(202, "Accepted"),
	NonAuthoritativeInformation(203, "Non-Authoritative Information"),
	NoContent(204, "No Content"),
	ResetContent(205, "Reset Content"),
	PartialContent(206, "Partial Content"),
	MultiStatus(207, "Multi-Status"),
	AlreadyReported(208, "Already Reported"),
	IMUsed(226, "IM Used"),
	AuthenticationSuccessful(230, "Authentication Successful"),
	MultipleChoices(300, "Multiple Choices"),
	MovedPermanently(301, "Moved Permanently"),
	Found(302, "Found"),
	SeeOther(303, "See Other"),
	NotModified(304, "Not Modified"),
	UseProxy(305, "Use Proxy"),
	SwitchProxy(306, "Switch Proxy"),
	TemporaryRedirect(307, "Temporary Redirect"),
	PermanentRedirect(308, "Permanent Redirect"),
	BadRequest(400, "Bad Request"),
	Unauthorized(401, "Unauthorized"),
	PaymentRequired(402, "Payment Required"),
	Forbidden(403, "Forbidden"),
	NotFound(404, "Not Found"),
	MethodNotAllowed(405, "Method Not Allowed"),
	NotAcceptable(406, "Not Acceptable"),
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
	RequestTimeout(408, "Request Timeout"),
	Conflict(409, "Conflict"),
	Gone(410, "Gone"),
	LengthRequired(411, "Length Required"),
	PreconditionFailed(412, "Precondition Failed"),
	RequestEntityTooLarge(413, "Request Entity Too Large"),
	RequestURITooLong(414, "Request-URI Too Long"),
	UnsupportedMediaType(415, "Unsupported Media Type"),
	RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),
	ExpectationFailed(417, "Expectation Failed"),
	ImATeapot(418, "I'm a teapot"),
	EnhanceYourCalm(420, "Enhance Your Calm"),
	UnprocessableEntity(422, "Unprocessable Entity"),
	Locked(423, "Locked"),
	FailedDependency(424, "Failed Dependency"),
	UnorderedCollection(425, "Unordered Collection"),
	UpgradeRequired(426, "Upgrade Required"),
	PreconditionRequired(428, "Precondition Required"),
	TooManyRequests(429, "Too Many Requests"),
	RequestHeaderFieldsTooLarge(431, "Request Header Fields Too Large"),
	NoResponse(444, "No Response"),
	RetryWith(449, "Retry With"),
	BlockedByWindowsParentalControls(450, "Blocked by Windows Parental Controls"),
	UnavailableForLegalReasons(451, "Unavailable For Legal Reasons"),
	RequestHeaderTooLarge(494, "Request Header Too Large"),
	CertError(495, "Cert Error"),
	NoCert(496, "No Cert"),
	HTTPtoHTTPS(497, "HTTP to HTTPS"),
	ClientClosedRequest(499, "Client Closed Request"),
	InternalServerError(500, "Internal Server Error"),
	NotImplemented(501, "Not Implemented"),
	BadGateway(502, "Bad Gateway"),
	ServiceUnavailable(503, "Service Unavailable"),
	GatewayTimeout(504, "Gateway Timeout"),
	HTTPVersionNotSupported(505, "HTTP Version Not Supported"),
	VariantAlsoNegotiates(506, "Variant Also Negotiates"),
	InsufficientStorage(507, "Insufficient Storage"),
	LoopDetected(508, "Loop Detected"),
	BandwidthLimitExceeded(509, "Bandwidth Limit Exceeded"),
	NotExtended(510, "Not Extended"),
	NetworkAuthenticationRequired(511, "Network Authentication Required"),
	NetworkReadTimeoutError(598, "Network read timeout error"),
	NetworkConnectTimeoutError(599, "Network connect timeout error");

	private int code;
	private String reason;

	private StatusCode(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}

	public int getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return String.format("%d %s", code, reason);
	}
	
	public static StatusCode fromCode(int code){
		return Lookup.get(code);
	}

	private static final Map<Integer, StatusCode> Lookup = createLookup();
	private static Map<Integer, StatusCode> createLookup() {
		Map<Integer, StatusCode> map = new LinkedHashMap<Integer, StatusCode>();
		for (StatusCode statusCodes : StatusCode.values()) {
			map.put(statusCodes.code, statusCodes);
		}
		return map;
	}
}

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
package com.threewks.thundr.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.view.file.Disposition;
import com.threewks.thundr.view.file.FileView;

public class MailBuilderImpl implements MailBuilder {
	private Mailer mailer;
	private String subject;
	private Map<String, String> from = map();
	private Map<String, String> replyTo = map();
	private Map<String, String> to = map();
	private Map<String, String> cc = map();
	private Map<String, String> bcc = map();
	private Object body;
	private List<Attachment> attachments = new ArrayList<Attachment>();

	public MailBuilderImpl(Mailer mailer) {
		this.mailer = mailer;
	}

	protected MailBuilderImpl(Mailer mailer, Map<String, String> from, Map<String, String> replyTo, Map<String, String> to, Map<String, String> cc, Map<String, String> bcc, String subject,
			Object body, List<Attachment> attachments) {
		super();
		this.mailer = mailer;
		this.from = from;
		this.replyTo = replyTo;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.body = body;
		this.attachments = attachments;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T body() {
		return (T) body;
	}

	@Override
	public <T> MailBuilderImpl body(T body) {
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public void send() {
		mailer.send(this);
	}

	@Override
	public MailBuilderImpl subject(String subject) {
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public MailBuilderImpl from(String emailAddress) {
		return from(emailAddress, null);
	}

	@Override
	public MailBuilderImpl from(String emailAddress, String name) {
		Map<String, String> from = Collections.singletonMap(emailAddress, name);
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public MailBuilderImpl to(String emailAddress) {
		return to(emailAddress, null);
	}

	@Override
	public MailBuilderImpl to(String emailAddress, String name) {
		return to(Collections.singletonMap(emailAddress, name));
	}

	@Override
	public MailBuilderImpl to(Map<String, String> to) {
		to = merge(this.to, to);
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public MailBuilderImpl cc(String emailAddress) {
		return cc(emailAddress, null);
	}

	@Override
	public MailBuilderImpl cc(String emailAddress, String name) {
		return cc(Collections.singletonMap(emailAddress, name));
	}

	@Override
	public MailBuilderImpl cc(Map<String, String> cc) {
		cc = merge(this.cc, cc);
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public MailBuilderImpl bcc(String emailAddress) {
		return bcc(emailAddress, null);
	}

	@Override
	public MailBuilderImpl bcc(String emailAddress, String name) {
		return bcc(Collections.singletonMap(emailAddress, name));
	}

	@Override
	public MailBuilderImpl bcc(Map<String, String> bcc) {
		bcc = merge(this.bcc, bcc);
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public MailBuilderImpl replyTo(String email) {
		return replyTo(email, null);
	}

	@Override
	public MailBuilderImpl replyTo(String email, String name) {
		Map<String, String> replyTo = Collections.singletonMap(email, name);
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public Map.Entry<String, String> from() {
		return from.isEmpty() ? null : from.entrySet().iterator().next();
	}

	@Override
	public Map<String, String> to() {
		return map(this.to);
	}

	@Override
	public Map<String, String> cc() {
		return map(this.cc);
	}

	@Override
	public Map<String, String> bcc() {
		return map(bcc);
	}

	@Override
	public String subject() {
		return subject;
	}

	@Override
	public Map.Entry<String, String> replyTo() {
		return replyTo.isEmpty() ? null : replyTo.entrySet().iterator().next();
	}

	@Override
	public MailBuilderImpl attach(FileView view) {
		return this.attach(view.getFileName(), view, view.getDisposition());
	}

	@Override
	public MailBuilderImpl attach(String name, Object view, Disposition disposition) {
		List<Attachment> attachments = new ArrayList<Attachment>(this.attachments);
		attachments.add(new Attachment(name, view, disposition));
		return create(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}

	@Override
	public List<Attachment> attachments() {
		return new ArrayList<Attachment>(attachments);
	}

	/**
	 * Factory for creating maps for internal use
	 * 
	 * @return
	 */
	protected Map<String, String> map() {
		return new LinkedHashMap<String, String>();
	}

	/**
	 * Factory for creating maps with the given content for internal use
	 * 
	 * @return
	 */
	protected Map<String, String> map(Map<String, String> content) {
		return new LinkedHashMap<String, String>(content);
	}

	private Map<String, String> merge(Map<String, String> a, Map<String, String> b) {
		Map<String, String> map = map();
		map.putAll(a);
		map.putAll(b);
		return map;
	}

	/**
	 * Factory method for creating a new instance with the given content
	 * 
	 * @param mailer
	 * @param from
	 * @param replyTo
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param body
	 * @param attachments
	 * @return
	 */
	protected MailBuilderImpl create(Mailer mailer, Map<String, String> from, Map<String, String> replyTo, Map<String, String> to, Map<String, String> cc, Map<String, String> bcc, String subject,
			Object body, List<Attachment> attachments) {
		return new MailBuilderImpl(mailer, from, replyTo, to, cc, bcc, subject, body, attachments);
	}
}

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
package com.threewks.thundr.mail;

import java.util.List;
import java.util.Map;

import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverRegistry;

/**
 * A fluent api for building and sending an email.
 * 
 * Instances are obtained by invoking {@link Mailer#mail()}.
 */
public interface MailBuilder {
	/**
	 * Send the email using the current state of the builder
	 * 
	 * @throws MailException
	 */
	public void send() throws MailException;

	/**
	 * Returns the body content of the email previously set on the builder
	 * 
	 * @return
	 */
	public <T> T body();

	/**
	 * Set the body of the email. The given view can be any object for which a {@link ViewResolver} has been registered in the {@link ViewResolverRegistry}.
	 * 
	 * @param view
	 * @return
	 * @throws MailException
	 */
	public <T> MailBuilder body(T view) throws MailException;

	/**
	 * Get the subject of the email previously set on the builder
	 * 
	 * @return
	 */
	public String subject();

	/**
	 * Specify the subject of the email
	 * 
	 * @param subject
	 * @return
	 */
	public MailBuilder subject(String subject);

	/**
	 * Get the email address and name of the sender of the email
	 * 
	 * @return
	 */
	public Map.Entry<String, String> from();

	/**
	 * Set the email address of the sender of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder from(String email);

	/**
	 * Set the email address and name of the sender of the email
	 * 
	 * @param email
	 * @param name
	 * @return
	 */
	public MailBuilder from(String email, String name);

	/**
	 * Get the 'to' recipients of the email as a map of email addresses to names
	 * 
	 * @return
	 */
	public Map<String, String> to();

	/**
	 * Add the given email address to the 'to' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder to(String email);

	/**
	 * Add the given email address and name to the 'to' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder to(String email, String name);

	/**
	 * Add the given map of email address to names to the 'to' recipients of the email
	 * 
	 * @param to
	 * @return
	 */
	public MailBuilder to(Map<String, String> to);

	/**
	 * Get the 'cc' recipients of the email as a map of email addresses to names
	 * 
	 * @return
	 */
	public Map<String, String> cc();

	/**
	 * Add the given email address to the 'cc' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder cc(String email);

	/**
	 * Add the given email address and name to the 'cc' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder cc(String email, String name);

	/**
	 * Add the given map of email address to names to the 'cc' recipients of the email
	 * 
	 * @param cc
	 * @return
	 */
	public MailBuilder cc(Map<String, String> cc);

	/**
	 * Get the 'bcc' recipients of the email as a map of email addresses to names
	 * 
	 * @return
	 */
	public Map<String, String> bcc();

	/**
	 * Add the given email address to the 'bcc' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder bcc(String email);

	/**
	 * Add the given email address and name to the 'bcc' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder bcc(String email, String name);

	/**
	 * Add the given map of email address to names to the 'bcc' recipients of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder bcc(Map<String, String> bcc);

	/**
	 * Get the email address and name of the 'reply to' address of the email
	 * 
	 * @return
	 */
	public Map.Entry<String, String> replyTo();

	/**
	 * Set the email address of the 'reply to' address of the email
	 * 
	 * @param email
	 * @return
	 */
	public MailBuilder replyTo(String email);

	/**
	 * Set the email address and name of the 'reply to' address of the email
	 * 
	 * @param email
	 * @param name
	 * @return
	 */
	public MailBuilder replyTo(String email, String name);

	/**
	 * Attaches the attachment to the email.
	 *
	 * @param attachment the attachment
	 * @return
	 */
	public MailBuilder attach(Attachment attachment);

	/**
	 * Get the attachments of the email.
	 *
	 * @return a list of attachments
	 */
	public List<Attachment> attachments();
}

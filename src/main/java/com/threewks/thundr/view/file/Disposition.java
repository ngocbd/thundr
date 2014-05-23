package com.threewks.thundr.view.file;

public enum Disposition {
	Attachment("attachment"),
	Inline("inline");

	private String value;

	Disposition(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}

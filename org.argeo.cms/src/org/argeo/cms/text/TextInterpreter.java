package org.argeo.cms.text;

import javax.jcr.Session;

/** Convert from/to data layer to/from presentation layer. */
public interface TextInterpreter {
	public String read(Session session, String nodePath);

	public void write(Session session, String nodePath, String content);
}

package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.Session;

/** Convert from/to data layer to/from presentation layer. */
public interface TextInterpreter {
	public String raw(Node node);

	public String read(Session session, String nodePath);

	public Node write(Session session, String nodePath, String content);
}

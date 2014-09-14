package org.argeo.cms.text;

import javax.jcr.Node;

/** Convert from/to data layer to/from presentation layer. */
public interface TextInterpreter {
	public String raw(Node node);

	public String read(Node contextNode, String nodePath);

	public Node write(Node contextNode, String nodePath, String content);
}

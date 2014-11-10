package org.argeo.cms.text;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;

public interface TextViewer {
	public void setParagraphStyle(Paragraph paragraph, String style);

	public void deleteParagraph(Paragraph paragraph);

	public String getRawParagraphText(Paragraph paragraph);

	public void edit(Composite composite, Object caretPosition);

	public Section getMainSection();

	public TextInterpreter getTextInterpreter();

	public MouseListener getMouseListener();

	public CmsEditable getCmsEditable();
}

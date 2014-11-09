package org.argeo.cms.text;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

public interface TextViewer {
	public void setParagraphStyle(Paragraph paragraph, String style);

	public void deleteParagraph(Paragraph paragraph);

	public String getRawParagraphText(Paragraph paragraph);

	public void edit(Composite composite, Object caretPosition);

//	public void saveEdit();
//
//	public void splitEdit();
//
//	public void mergeWithPrevious();
//
//	public void mergeWithNext();
//
//	public void cancelEdit();

	public void layout(Composite composite);

	public Section getMainSection();

	public TextInterpreter getTextInterpreter();

	public CmsEditable getCmsEditable();
}

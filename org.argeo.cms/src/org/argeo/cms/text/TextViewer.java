package org.argeo.cms.text;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

public interface TextViewer {
	public void setParagraphStyle(Paragraph paragraph, String style);

	public void deleteParagraph(Paragraph paragraph);

	public String getRawParagraphText(Paragraph paragraph);

	public void edit(Composite composite, Object caretPosition);

	//protected void prepare(StyledComposite st, Object caretPosition);

	public void saveEdit();

	public void splitEdit();

	public void mergeWithPrevious();

	public void mergeWithNext();

	public void cancelEdit();

	//protected void stopEditing(Boolean save) throws RepositoryException;

	public void deepen();

	public void undeepen();

	public void layout(Composite composite);

	public Section getMainSection();

	public TextInterpreter getTextInterpreter();

	public CmsEditable getCmsEditable();

	public StyledTools getStyledTools();

}

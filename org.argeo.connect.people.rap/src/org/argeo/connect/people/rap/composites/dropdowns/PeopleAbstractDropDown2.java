package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.Arrays;
import java.util.List;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.widgets.DropDown;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public abstract class PeopleAbstractDropDown2 {

	private final Text text;
	private final DropDown dropDown;
	private boolean modifyFromList = false;

	// Current displayed text
	private String userText = "";
	// Current displayed list items
	private String[] values;

	// Implementers should call refreshValues() once init has been done.
	public PeopleAbstractDropDown2(Text text) {
		dropDown = new DropDown(text);
		dropDown.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		this.text = text;
		addListeners();
	}

	public String getText() {
		return text.getText();
	}

	public void init() {
		// Workaround the dropDown show issue initialising the drop down
		List<String> filteredValues = getFilteredValues(text.getText());
		values = filteredValues.toArray(new String[filteredValues.size()]);
		dropDown.setItems(values);
	}

	public void reset(String value) {
		// Workaround the dropDown show issue when resetting the text
		modifyFromList = true;
		if (CommonsJcrUtils.checkNotEmptyString(value))
			text.setText(value);
		else
			text.setText("");
		refreshValues();
		modifyFromList = false;
	}

	/** Overwrite to provide specific filtering */
	protected abstract List<String> getFilteredValues(String filter);

	protected void refreshValues() {
		List<String> filteredValues = getFilteredValues(text.getText());
		values = filteredValues.toArray(new String[filteredValues.size()]);
		dropDown.setItems(values);
		if (!modifyFromList)
			// Force show on focus in
			// FIXME LEGACY METHOD THAT HAS BEEN REMOVED
			dropDown.setVisible(true);

	}

	protected void addListeners() {
		addModifyListener();
		addSelectionListener();
		addDefaultSelectionListener();
		addFocusListener();

	}

	private void addFocusListener() {
		text.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = -7179112097626535946L;

			public void focusGained(FocusEvent event) {
				dropDown.setVisible(true);
			}

			public void focusLost(FocusEvent event) {
				dropDown.setVisible(false);
				if (!Arrays.asList(values).contains(userText)) {
					modifyFromList = true;
					text.setText("");
					refreshValues();
					modifyFromList = false;
				}
			}
		});
	}

	private void addSelectionListener() {
		dropDown.addListener(SWT.Selection, new Listener() {
			private static final long serialVersionUID = -2357157809365135142L;

			public void handleEvent(Event event) {
				if (event.index != -1) {
					modifyFromList = true;
					text.setText(values[event.index]);
					modifyFromList = false;
					text.selectAll();
				} else {
					text.setText(userText);
					text.setSelection(userText.length(), userText.length());
					text.setFocus();
				}
			}
		});
	}

	private void addDefaultSelectionListener() {
		dropDown.addListener(SWT.DefaultSelection, new Listener() {
			private static final long serialVersionUID = -5958008322630466068L;

			public void handleEvent(Event event) {
				if (event.index != -1) {
					text.setText(values[event.index]);
					text.setSelection(event.text.length());
					dropDown.setVisible(false);
				}
			}
		});
	}

	private void addModifyListener() {
		text.addListener(SWT.Modify, new Listener() {
			private static final long serialVersionUID = -4373972835244263346L;

			public void handleEvent(Event event) {
				if (!modifyFromList) {
					refreshValues();
					if (values.length == 1) {
						dropDown.setSelectionIndex(0);
					}
					dropDown.setVisible(true);
				}
			}
		});
	}

	// text.addFocusListener(new FocusListener() {
	// private static final long serialVersionUID = 1L;
	//
	// // TODO clean this, it is not the best way to force display of
	// // the list when the text is empty, and has some weird side
	// // effects.
	// @Override
	// public void focusLost(FocusEvent event) {
	// }
	//
	// @Override
	// public void focusGained(FocusEvent event) {
	// // Force show on focus in
	// // FIXME LEGACY METHOD THAT HAS BEEN REMOVED
	// dropDown.setVisible(true);
	// }
	// });
	//
	// text.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void modifyText(ModifyEvent event) {
	// // Avoid reducing suggestion while browsing them
	// if (!modifyFromList) {
	// refreshValues();
	// }
	// }
	// });
	//
	// // text.addListener(SWT.Traverse, new DDReturnListener());
	// // dropDown.addListener(SWT.CANCEL, new AllEventsListener());
	// // dropDown.addListener(SWT.CLOSE, new AllEventsListener());
	// // dropDown.addListener(SWT.CR, new AllEventsListener());
	// // dropDown.addListener(SWT.Selection, new AllEventsListener());
	// dropDown.addListener(SWT.Selection, new DDSelectionListener());
	// dropDown.addListener(SWT.DefaultSelection, new DDSelectionListener());
	// }

	//
	// private class AllEventsListener implements Listener {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void handleEvent(Event event) {
	// System.out.println("Event received: " + event.type);
	// }
	// }

	// private class DDReturnListener implements Listener {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void handleEvent(Event event) {
	// event.doit = false;
	// System.out.println("RETURN HIT");
	// }
	// }

	// private class DDSelectionListener implements Listener {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void handleEvent(Event event) {
	//
	// modifyFromList = true;
	// int index = dropDown.getSelectionIndex();
	// if (index != -1 && index < values.length)
	// text.setText(values[index]);
	// modifyFromList = false;
	// if (event.type == SWT.DefaultSelection)
	// KeyboardFocusManager.getCurrentKeyboardFocusManager()
	// .focusNextComponent();
	// // event.doit = false;
	// }
	// }
}
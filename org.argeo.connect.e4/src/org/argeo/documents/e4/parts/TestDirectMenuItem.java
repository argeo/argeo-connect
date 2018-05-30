package org.argeo.documents.e4.parts;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

public class TestDirectMenuItem {
	@Execute
	public void execute() {
		System.out.println("execute TestDirectMenuItem");
	}
	
	@CanExecute
	public boolean canExecute() {
		return false;
	}
}

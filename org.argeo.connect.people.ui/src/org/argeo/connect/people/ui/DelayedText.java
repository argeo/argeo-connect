package org.argeo.connect.people.ui;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * Text that introduce a timer in the attached ModifyListener.
 * 
 * Note that corresponding ModifyEvent will *NOT* be sent in the UI thread.
 * Calling ModifierInstance must be implemented in consequence. Not also that
 * this delayed text will only manage one listener for the time being
 *
 */
public class DelayedText extends Text {
	private static final long serialVersionUID = 5415229346092946190L;

	final int delay;
	private Object lock = new Object();
	private MyTimer timer = new MyTimer(DelayedText.this.toString());
	private ModifyListener delayedModifyListener;
	private ServerPushSession pushSession;

	public DelayedText(Composite parent, int style, int delayInMs) {
		super(parent, style);
		this.delay = delayInMs;
		this.addModifyListener(modifyListener);
	}

	private ModifyListener modifyListener = new ModifyListener() {
		private static final long serialVersionUID = 1117506414462641980L;

		public void modifyText(ModifyEvent e) {
			ModifyEvent delayedEvent = null;
			synchronized (lock) {
				if (delayedModifyListener != null) {
					Event tmpEvent = new Event();
					tmpEvent.widget = DelayedText.this;
					tmpEvent.display = e.display;
					tmpEvent.data = e.data;
					tmpEvent.time = e.time;
					delayedEvent = new ModifyEvent(tmpEvent);
				}
			}
			final ModifyEvent timerModifyEvent = delayedEvent;

			synchronized (timer) {
				if (timer.timerTask != null) {
					timer.timerTask.cancel();
					timer.timerTask = null;
				}

				if (delayedEvent != null) {
					timer.timerTask = new TimerTask() {
						public void run() {
							synchronized (lock) {
								delayedModifyListener
										.modifyText(timerModifyEvent);
								// Bad approach: it is not a good idea to put a
								// display.asyncExec in a lock...
								// DelayedText.this.getDisplay().asyncExec(new
								// Runnable() {
								// @Override
								// public void run() {
								// delayedModifyListener.modifyText(timerModifyEvent);
								// }
								// }
								// );
							}
							synchronized (timer) {
								timer.timerTask = null;
							}
						}
					};
					timer.schedule(timer.timerTask, delay);
					pushSession.start();
				}
			}
		};
	};

	/**
	 * Adds a modify text listener that will be delayed. If another Modify event
	 * happens during the waiting delay, the older event will be canceled an a
	 * new one will be scheduled after another new delay.
	 */
	public void addDelayedModifyListener(ServerPushSession pushSession,
			ModifyListener listener) {
		synchronized (lock) {
			delayedModifyListener = listener;
			this.pushSession = pushSession;
		}
	}

	public void removeDelayedModifyListener(ModifyListener listener) {
		synchronized (lock) {
			delayedModifyListener = null;
			pushSession = null;
		}
	}

	private class MyTimer extends Timer {
		private TimerTask timerTask = null;

		public MyTimer(String name) {
			super(name);
		}
	};
}
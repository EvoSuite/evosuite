package org.exsyst.run;

import java.awt.Container;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;
import org.uispec4j.interception.toolkit.UISpecToolkit;

import org.exsyst.model.states.UIState;
import org.exsyst.model.states.UIStateGraph;

public class UIEnvironment extends AbstractUIEnvironment implements InterceptionHandler {
	private static final int noWindowsVisibleSleepTimeMs = 1;
	private static final int noWindowsVisibleTimeoutMs = 3000;

	private List<Window> windows = Collections.synchronizedList(new LinkedList<Window>());
	private UISpecDisplay display;
	private InterceptionHandler delegate;
	private List<InterceptionHandler> modalWindowHandlers = Collections.synchronizedList(new LinkedList<InterceptionHandler>());
	private Set<java.awt.Window> initialWindows = new HashSet<java.awt.Window>(); 
	private Set<Thread> initialThreads = new HashSet<Thread>();
	
	public UIEnvironment(UISpecDisplay display, InterceptionHandler delegate) {
		// display.reset();
		
		this.display = display;
		this.delegate = delegate;

		for (int i = 0; i < 100; i++) {
			this.register();
		}

		this.initialThreads = currentThreads();
		
		// this.initialWindows = new HashSet<java.awt.Window>(Arrays.asList(java.awt.Window.getWindows()));
	}

	public static Set<Thread> currentThreads() {
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		
		List<Thread> list = new LinkedList<Thread>(Arrays.asList(threads));
		while (list.remove(null));
		
 		return new HashSet<Thread>(list);
	}

	public void register() {
		this.display.add(this);
	}

	public List<Window> getWindows() {
		return Collections.unmodifiableList(this.windows);
	}
	
	@Override
	public List<Window> getTargetableWindows() {
		List<Window> activeWindows = new LinkedList<Window>();
		
		Window lastModal = null;

		synchronized(this.windows) {
			for (Window w : this.windows) {
				final Container c = w.getAwtComponent();
				boolean isPopUp = FocusOrder.isPopUpWindow(w); 
				boolean isPopUpEnabled = isPopUp && FocusOrder.isPopUpWindowEnabled(w);
	
				if (c.isEnabled() && c.isVisible() && isPopUp && !isPopUpEnabled) {
					this.disposePopup(w);
				}
				
				if (c.isEnabled() && c.isVisible() && (!isPopUp || isPopUpEnabled)) { 
					activeWindows.add(w);
					
					if (isModal(c) || isPopUp) {
						lastModal = w;
					}
				}
			}
		}
			
		return Collections.unmodifiableList(lastModal != null ? Arrays.asList(lastModal) : activeWindows);
	}
	
	public static boolean isModal(Container window) {
		return !(window instanceof JDialog) ? false : ((JDialog) window).isModal();
	}

	public void disposeOpenPopups() {
		// Dispose all popups after execution of an action
		synchronized (this.windows) {
			for (Window w : this.windows) {
				if (FocusOrder.isPopUpWindow(w)) {
					this.disposePopup(w);
				}
			}
		}
	}
	
	private void disposePopup(Window w) {
		w.dispose();
		w.getAwtComponent().setVisible(false);
	}

	@Override
	public void process(final Window window) {
		// Readd ourself as window processor
		this.register();

		// System.out.println("UIEnvironment::process: window = " + window.getAwtComponent() + ": isVisible = " + window.getAwtComponent().isVisible());
		assert(window.getAwtComponent().isVisible());
		
		if (window.isModal().isTrue()) {
			synchronized (this.modalWindowHandlers) {
				if (this.modalWindowHandlers.size() > 0) {
					InterceptionHandler handler = this.modalWindowHandlers.remove(this.modalWindowHandlers.size() - 1);
					handler.process(window);
				}
			}
		}

		try {
			// The thread we are called from at this point seems to be holding the AWT-Tree-Lock --
			// if we were to call our delegate directly it would be owning the AWT-Tree-Lock --
			// meaning that it would block its own SwingUtilities.invokeLater() calls!
			//
			// We can also not execute the delegate logic on the Swing thread because then the
			// delegate would still be blocking its own SwingUtilities.invokeLater() calls...
			//
			// So what we do is to create a new thread instead.
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						UIEnvironment.this.disposeOpenPopups();
						UIEnvironment.this.windows.add(window);
						UIEnvironment.this.delegate.process(window);
					} catch (Throwable t) {
						System.err.println("Unhandled exception in " + Thread.currentThread() + ":");
						t.printStackTrace();
						System.err.flush();
					}
				}
			};
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (Thread.currentThread().getName().contains("UIEnvironment helper thread")) {
						runnable.run();
					} else {
						new Thread(runnable, "UIEnvironment helper thread for processing window without locks").start();
					}
				}
			});
			
		} catch (Throwable t) {
			System.out.println("UIEnvironment::process(Window) caught throwable:");
			t.printStackTrace();
		}
	}

	@Override
	public UIState waitGetNewState(UIStateGraph stateGraph) {
		// System.out.println("waitGetNewState inThread: " + Thread.currentThread());

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) { /* OK */ }
				}
			});
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
		List<Window> windows = null;
		
		long startTime = System.currentTimeMillis();
		
		do { 
			if (System.currentTimeMillis() - startTime > noWindowsVisibleTimeoutMs) {
				break;
			}
			
			if (windows != null) {
				// System.out.println("No windows visible from waitGetNewState(): Waiting...");
			}

			try {
				Thread.sleep(noWindowsVisibleSleepTimeMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			windows = this.getTargetableWindows();
		} while (windows.isEmpty());

		return stateGraph.stateForEnvironment(this);
	}

	@Override
	public void dispose() {		
		Set<java.awt.Window> windows = new HashSet<java.awt.Window>(Arrays.asList(java.awt.Window.getWindows()));
		windows.removeAll(this.initialWindows);
		
		for (java.awt.Window window : windows) {
			window.dispose();
			window.setVisible(false);
		}
		
		Set<Thread> threads = currentThreads();
		threads.removeAll(this.initialThreads);
		
		while (this.display.remove(this));
		UISpecToolkit.instance().resetSystemClipboard();
		
		/* for (Thread t : threads) {
			System.err.println("Interrupting thread " + t);
			t.interrupt();
			t.stop();
		} */
		
/*		try {
			ThreadManager threadManager = ThreadManager.getInstance();
			
			Field field = ThreadManager.class.getDeclaredField("threads");
			field.setAccessible(true);
			List<Thread> threads = new ArrayList<Thread>((List<Thread>) field.get(threadManager));			
			
			for (Thread thread : threads) {
				thread.stop();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}*/
	}

	@Override
	public void registerModalWindowHandler(InterceptionHandler handler) {
		this.modalWindowHandlers.add(handler);
	}

	@Override
	public void unregisterModalWindowHandler(InterceptionHandler handler) {
		this.modalWindowHandlers.remove(handler);
	}
}

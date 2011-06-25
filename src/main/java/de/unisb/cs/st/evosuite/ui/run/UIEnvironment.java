package de.unisb.cs.st.evosuite.ui.run;

import java.awt.Container;
import java.util.*;

import javax.swing.SwingUtilities;

import org.uispec4j.Window;
import org.uispec4j.interception.handlers.InterceptionHandler;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import de.unisb.cs.st.evosuite.ui.model.states.UIState;
import de.unisb.cs.st.evosuite.ui.model.states.UIStateGraph;

public class UIEnvironment extends AbstractUIEnvironment implements InterceptionHandler {
	private static final int noWindowsVisibleSleepTimeMs = 100;
	private List<Window> windows = Collections.synchronizedList(new LinkedList<Window>());
	private UISpecDisplay display;
	private InterceptionHandler delegate;
	private List<InterceptionHandler> modalWindowHandlers = Collections.synchronizedList(new LinkedList<InterceptionHandler>());

	public UIEnvironment(UISpecDisplay display, InterceptionHandler delegate) {
		// display.reset();
		
		this.display = display;
		this.delegate = delegate;

		this.register();
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

		for (Window w : this.windows) {
			final Container c = w.getAwtComponent();
			
			if (c.isEnabled() && c.isVisible()) { 
				activeWindows.add(w);
				
				if (w.isModal().isTrue()) {
					lastModal = w;
				}
			}
		}

		return Collections.unmodifiableList(lastModal != null ? Arrays.asList(lastModal) : activeWindows);
	}

	@Override
	public synchronized void process(final Window window) {
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
			// Readd ourself as window processor
			this.register();
	
			this.windows.add(window);
			
			// The thread we are called from at this point seems to be holding the AWT-Tree-Lock --
			// if we were to call our delegate directly it would be owning the AWT-Tree-Lock --
			// meaning that it would block its own SwingUtilities.invokeLater() calls!
			//
			// We can also not execute the delegate logic on the Swing thread because then the
			// delegate would still be blocking its own SwingUtilities.invokeLater() calls...
			//
			// So what we do is to create a new thread instead.
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						UIEnvironment.this.delegate.process(window);
					} catch (Exception e) {
						System.out.println("Unhandled exception in " + Thread.currentThread() + ":");
						e.printStackTrace();
					}
				}
			};
			
			if (Thread.currentThread().getName().contains("UIEnvironment helper thread")) {
				runnable.run();
			} else {
				new Thread(runnable, "UIEnvironment helper thread for processing window without locks").start();
			}
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
						Thread.sleep(100);
					} catch (InterruptedException e) { /* OK */ }
				}
			});
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
		List<Window> windows = null;
		
		do { 
			if (windows != null) {
				System.out.println("No windows visible from waitGetNewState(): Waiting...");
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
/*		for (Window window : this.windows) {
			window.dispose();
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

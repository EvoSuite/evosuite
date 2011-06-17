package de.unisb.cs.st.evosuite.ma;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author Yury Pavlov
 */
public class SimpleGUI implements IGUI {

	


	public void createWindow() {
		final JFrame f = new JFrame("Editot");
		final JButton b = new JButton("Send false");
		final Object lock = new Object();
		// EventQueue.invokeLater(new Runnable() {
		// public void run() {
		f.setSize(400, 300);
		f.setLayout(new FlowLayout());
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		f.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.out.println("Closing window");
				f.setVisible(false);
				synchronized (lock) {
					lock.notifyAll();
				}
			}

		});

		f.add(b);
		System.out.println("Thread2: " + Thread.currentThread());
		f.setVisible(true);
		// }
		// });

		synchronized (lock) {
			while (f.isVisible())
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

	}
}

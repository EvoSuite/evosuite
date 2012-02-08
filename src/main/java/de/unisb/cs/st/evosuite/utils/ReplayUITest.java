package de.unisb.cs.st.evosuite.utils;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.UIManager;

import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;

import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.ChromosomeUIController;
import de.unisb.cs.st.evosuite.ui.genetics.UITestChromosome;

public class ReplayUITest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("uispec4j.test.library", "junit");

		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Really, really important, otherwise event loops get killed
		// randomly!
		AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread());
		// Also important to do this very early (here), otherwise we might
		// end up with multiple event loops from loading classes
		UISpec4J.init();

		String filename = null;

		if (args.length > 0) {
			filename = args[0];
		}

		if (filename == null) {
			filename = "solution.obj";
		}

		Object obj = readObjectFromFile(filename);
		@SuppressWarnings("unchecked")
		AbstractTestSuiteChromosome<UITestChromosome> solution = (AbstractTestSuiteChromosome<UITestChromosome>) obj;

		int testIdx = 1;

		for (UITestChromosome test : solution.getTestChromosomes()) {
			System.out.println(String.format("Replaying test %d of %d...", testIdx, solution.getTestChromosomes().size()));

			try {
				new ChromosomeUIController(test).call();
			} catch (Exception e) {
				System.out.println("Got exception in initialization random walk:");
				e.printStackTrace();
			}

			testIdx++;
		}

		System.out.println("Finished!");
		AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
		System.exit(0);
	}

	public static Object readObjectFromFile(String filename) {
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(filename));
			Object result = oos.readObject();
			oos.close();
			return result;
		} catch (Throwable t) {
			System.out.println("Exception on reading object:");
			t.printStackTrace();
			return null;
		}
	}

}

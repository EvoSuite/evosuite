package org.evosuite.intellij.util;

/**
 * EvoSuite is run on background, in a non-blocking way.
 * Once finished, we might want some sort of notification
 *
 * Created by arcuri on 10/2/14.
 */
public interface AsyncGUINotifier {

    public void success(String message);

    public void failed(String message);

    public void attachProcess(Process process);

    public void printOnConsole(String message);

    public void clearConsole();
}

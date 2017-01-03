/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import org.evosuite.intellij.util.EvoVersion;
import org.evosuite.intellij.util.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvoStartDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JFormattedTextField memoryField;
    private JFormattedTextField coreField;
    private JFormattedTextField timeField;
    private JTextField folderField;
    private JTextField mavenField;
    private JTextField javaHomeField;
    private JButton selectMavenButton;
    private JButton selectJavaHomeButton;
    private JTextField evosuiteLocationTesxField;
    private JButton evosuiteSelectionButton;
    private JRadioButton mavenRadioButton;
    private JRadioButton evosuiteRadioButton;

    private volatile boolean wasOK = false;
    private volatile EvoParameters params;
    private volatile Project project;

    public void initFields(Project project, EvoParameters params) {
        this.project = project;
        this.params = params;

        coreField.setValue(params.getCores());
        memoryField.setValue(params.getMemory());
        timeField.setValue(params.getTime());

        folderField.setText(params.getFolder());
        mavenField.setText(params.getMvnLocation());
        evosuiteLocationTesxField.setText(params.getEvosuiteJarLocation());
        javaHomeField.setText(params.getJavaHome());

        if (!Utils.isMavenProject(project)) {
            //disable Maven options
            selectMavenButton.setEnabled(false);
            mavenRadioButton.setEnabled(false);
            params.setExecutionMode(EvoParameters.EXECUTION_MODE_JAR);
        }

        if (params.usesMaven()) {
            mavenRadioButton.setSelected(true);
        } else {
            evosuiteRadioButton.setSelected(true);
        }
        checkExecution();
    }


    public EvoStartDialog() {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        selectMavenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onSelectMvn();
            }
        });
        selectJavaHomeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onSelectJavaHome();
            }
        });
        selectJavaHomeButton.setToolTipText("Choose a valid JDK home for Java " + EvoVersion.JAVA_VERSION);

        mavenRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                checkExecution();
            }
        });
        evosuiteRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                checkExecution();
            }
        });
        evosuiteSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onSelectEvosuite();
            }
        });

        setPreferredSize(new Dimension(EvoParameters.getInstance().getGuiWidth(), EvoParameters.getInstance().getGuiHeight()));
    }

    private void onSelectEvosuite() {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // need to be able to navigate through folders
                }
                return checkIfValidEvoSuiteJar(file);
            }

            @Override
            public String getDescription() {
                return "EvoSuite executable jar";
            }
        });

        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            params.setEvosuiteJarLocation(path);
            evosuiteLocationTesxField.setText(path);
        }
    }

    private void checkExecution() {
        if (mavenRadioButton.isSelected()) {
            params.setExecutionMode(EvoParameters.EXECUTION_MODE_MVN);
        } else if (evosuiteRadioButton.isSelected()) {
            params.setExecutionMode(EvoParameters.EXECUTION_MODE_JAR);
        }

        evosuiteLocationTesxField.setEnabled(evosuiteRadioButton.isSelected());
        evosuiteSelectionButton.setEnabled(evosuiteRadioButton.isSelected());
        mavenField.setEnabled(mavenRadioButton.isSelected());
        selectMavenButton.setEnabled(mavenRadioButton.isSelected());
    }

    private void onSelectMvn() {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // need to be able to navigate through folders
                }
                return checkIfValidMaven(file);
            }

            @Override
            public String getDescription() {
                return "Maven executable";
            }
        });

        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            params.setMvnLocation(path);
            mavenField.setText(path);
        }
    }

    private void onSelectJavaHome() {

        String jdkStartLocation = getJDKStartLocation();

        JFileChooser fc = new JFileChooser(jdkStartLocation);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                //return checkIfValidJavaHome(file);
                return true; //otherwise all folders will be greyed out
            }

            @Override
            public String getDescription() {
                return "Java Home (containing bin/javac)";
            }
        });
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();

            if (!checkIfValidJavaHome(new File(path))) {
                Messages.showMessageDialog(project, "Invalid JDK home: choose a correct one that contains bin/javac",
                        "EvoSuite Plugin", Messages.getErrorIcon());
                return;
            }

            params.setJavaHome(path);
            javaHomeField.setText(path);
        }
    }

    private String getJDKStartLocation() {
        String start = params.getJavaHome(); //TODO check for null
        if (start == null || start.isEmpty() && ProjectJdkTable.getInstance().getAllJdks().length > 0) {
            //try to check the JDK used by the project

            for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
                if (sdk.getVersionString().contains("" + EvoVersion.JAVA_VERSION)) {
                    start = sdk.getHomePath();
                    break;
                }
            }
            if (start == null) {
                //just take something as starting point
                start = ProjectJdkTable.getInstance().getAllJdks()[0].getHomePath();
            }
        }

        if (start == null || start.isEmpty()) {
            return ""; //if still empty, return default ""
        }

        File file = new File(start);
        while (file != null) {
            if (file.getName().toLowerCase().contains("jdk")) {
                file = file.getParentFile(); //go to the parent, which might have several JDKs
                break;
            }
            file = file.getParentFile();
        }

        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return "";
        }
    }

    private boolean checkIfValidJavaHome(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return false;
        }

        String javac = Utils.isWindows() ? "javac.exe" : "javac";
        File jf = new File(new File(file, "bin"), javac);
        return jf.exists();
    }

    private boolean checkIfValidEvoSuiteJar(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        String name = file.getName().toLowerCase();

        if(Arrays.asList("runtime","standalone","client","plugin","test","generated").stream()
                .anyMatch(k -> name.contains(k))){
            return false;
        }

        return name.startsWith("evosuite") && name.endsWith(".jar");
    }

    private boolean checkIfValidMaven(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        List<String> names = Utils.getMvnExecutableNames();
        for (String name : names) {
            if (file.getName().toLowerCase().equals(name)) {
                return true;
            }
        }
        return false;
    }


    private void onOK() {
// add your code here

        dispose();

        wasOK = saveParameters(true);
    }

    private void onCancel() {
// add your code here if necessary


        dispose();

        saveParameters(false);
    }


    private boolean saveParameters(boolean validate) {

        int cores = ((Number) coreField.getValue()).intValue();
        int memory = ((Number) memoryField.getValue()).intValue();
        int time = ((Number) timeField.getValue()).intValue();
        String dir = folderField.getText();
        String mvn = mavenField.getText();
        String javaHome = javaHomeField.getText();
        String evosuiteJar = evosuiteLocationTesxField.getText();


        List<String> errors = new ArrayList<>();

        if (cores < 1) {
            errors.add("Number of cores needs to be positive value");
        } else {
            params.setCores(cores);
        }

        if (memory < 1) {
            errors.add("Memory needs to be a positive value");
        } else {
            params.setMemory(memory);
        }

        if (time < 1) {
            errors.add("Duration needs to be a positive value");
        } else {
            params.setTime(time);
        }

        if (params.usesMaven() && !checkIfValidMaven(new File(mvn))) {
            errors.add("Invalid Maven executable: choose a correct one");
        } else {
            params.setMvnLocation(mvn);
        }

        if (!params.usesMaven() && !checkIfValidEvoSuiteJar(new File(evosuiteJar))) {
            errors.add("Invalid EvoSuite executable jar: choose a correct evosuite*.jar one");
        } else {
            params.setEvosuiteJarLocation(evosuiteJar);
        }

        if (!checkIfValidJavaHome(new File(javaHome))) {
            errors.add("Invalid JDK home: choose a correct one that contains bin/javac");
        } else {
            params.setJavaHome(javaHome);
        }

        params.setFolder(dir);
        params.setGuiWidth(this.getWidth());
        params.setGuiHeight(this.getHeight());


        if (validate && !errors.isEmpty()) {
            String title = "ERROR: EvoSuite Plugin";
            String msg = String.join("\n",errors);
            Messages.showMessageDialog(project, msg, title, Messages.getErrorIcon());
            return false;
        }

        return true;
    }


    public static void main(String[] args) {
        EvoStartDialog dialog = new EvoStartDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setParseIntegerOnly(true);
        coreField = new JFormattedTextField(nf);
        memoryField = new JFormattedTextField(nf);
        timeField = new JFormattedTextField(nf);
    }

    public boolean isWasOK() {
        return wasOK;
    }
}

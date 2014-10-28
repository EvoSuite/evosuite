package org.evosuite.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.*;
import java.text.NumberFormat;

public class EvoStartDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JFormattedTextField memoryField;
    private JFormattedTextField coreField;
    private JFormattedTextField timeField;
    private JTextField folderField;

    private volatile boolean wasOK = false;
    private volatile EvoParameters params;
    private volatile Project project;

    public void initFields(Project project, EvoParameters params){
        this.project = project;
        this.params = params;
        coreField.setText(""+params.getCores());
        memoryField.setText(""+params.getMemory());
        timeField.setText(""+params.getTime());
        folderField.setText(params.getFolder());
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
    }

    private void onOK() {
// add your code here

        dispose();

        int cores = Integer.parseInt(coreField.getText());
        int memory = Integer.parseInt(memoryField.getText());
        int time = Integer.parseInt(timeField.getText());
        String dir = folderField.getText();

        if (cores < 1 || memory < 1 || time < 1) {
            Messages.showMessageDialog(project, "Parameters need positive values",
                    "EvoSuite Plugin", Messages.getErrorIcon());
            return;
        }

        params.setCores(cores);
        params.setMemory(memory);
        params.setTime(time);
        params.setFolder(dir);

        wasOK = true;
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        EvoStartDialog dialog = new EvoStartDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        coreField = new JFormattedTextField(NumberFormat.getNumberInstance());
        memoryField = new JFormattedTextField(NumberFormat.getNumberInstance());
        timeField = new JFormattedTextField(NumberFormat.getNumberInstance());
    }

    public boolean isWasOK() {
        return wasOK;
    }
}

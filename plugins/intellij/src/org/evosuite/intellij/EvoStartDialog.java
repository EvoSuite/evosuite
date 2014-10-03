package org.evosuite.intellij;

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

    public void initFields(EvoParameters params){
        this.params = params;
        coreField.setText(""+params.cores);
        memoryField.setText(""+params.memory);
        timeField.setText(""+params.time);
        folderField.setText(params.folder);
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
        wasOK = true;
        //TODO update params
        dispose();
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

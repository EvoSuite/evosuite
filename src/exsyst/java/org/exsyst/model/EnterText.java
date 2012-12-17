package org.exsyst.model;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.primitives.ConstantPool;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.utils.Randomness;
import org.exsyst.run.AbstractUIEnvironment;
import org.uispec4j.TextBox;
import org.uispec4j.UIComponent;

public class EnterText extends UIAction<TextBox> {
	public static enum Mode {
		EnterText, AppendText, InsertText
	}

	private static final long serialVersionUID = 1L;

	private String text;
	private Mode mode;
	private double posRand;

	@Override
	public void executeOn(AbstractUIEnvironment env, final TextBox textBox) {
		this.checkTarget(textBox);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				Mode pseudoMode = EnterText.this.mode;
				JComponent jComp = textBox.getAwtComponent();

				int length = textBox.getText().length();
				
				// This is to avoid Exceptions with empty components
				if (jComp instanceof JTextComponent) {
					JTextComponent jTComp = (JTextComponent) jComp;
					length = jTComp.getDocument().getLength();
				}

				if (length <= 0) {
					pseudoMode = Mode.EnterText;
				}
				
				switch (pseudoMode) {
				case EnterText:
					textBox.setText(EnterText.this.text);
					break;

				case AppendText:
					textBox.appendText(EnterText.this.text);
					break;

				case InsertText:					
					textBox.insertText(EnterText.this.text,
					                   (int) (EnterText.this.posRand * length));
					break;

				default:
					throw new IllegalStateException("Unknown EnterText.Mode "
					        + EnterText.this.mode);
				}
			}
		});
	}

	@Override
	public boolean randomize() {
		boolean changed = false;

		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
			this.text = Randomness.nextString(Randomness.nextInt(Properties.STRING_LENGTH));
		else {
			ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
			this.text = constantPool.getRandomString();
		}

		this.mode = Randomness.choice(Mode.values());
		this.posRand = (this.mode == Mode.InsertText) ? Randomness.nextDouble() : 0;

		changed = true;

		super.randomize();
		return changed;
	}

	@Override
	public String toString() {
		String posStr = (this.mode == Mode.InsertText) ? String.format(", %.4f",
		                                                               this.posRand) : "";
		return String.format("%s[\"%s\"%s]", this.mode.toString(),
		                     StringEscapeUtils.escapeJava(this.text), posStr);
	}

	@Override
	public String graphVizString() {
		return "EnterText";
	}

	public static void addActions(List<UIAction<? extends UIComponent>> result) {
		result.add(new EnterText());
	}

}

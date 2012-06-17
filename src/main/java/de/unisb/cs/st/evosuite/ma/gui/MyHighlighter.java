/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.gui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class MyHighlighter extends DefaultHighlighter {

	private JTextComponent component;

	/**
	 * @see javax.swing.text.DefaultHighlighter#install(javax.swing.text.JTextComponent)
	 */
	@Override
	public final void install(final JTextComponent c) {
		super.install(c);
		this.component = c;
	}

	/**
	 * @see javax.swing.text.DefaultHighlighter#deinstall(javax.swing.text.JTextComponent)
	 */
	@Override
	public final void deinstall(final JTextComponent c) {
		super.deinstall(c);
		this.component = null;
	}

	/**
	 * Same algo, except width is not modified with the insets.
	 * 
	 * @see javax.swing.text.DefaultHighlighter#paint(java.awt.Graphics)
	 */
	@Override
	public final void paint(final Graphics g) {
		final Highlighter.Highlight[] highlights = getHighlights();
		final int len = highlights.length;
		for (int i = 0; i < len; i++) {
			Highlighter.Highlight info = highlights[i];
			if (info.getClass().getName().indexOf("LayeredHighlightInfo") > -1) {
				// Avoid allocing unless we need it.
				final Rectangle a = this.component.getBounds();
				final Insets insets = this.component.getInsets();
				a.x = insets.left;
				a.y = insets.top;
				// a.width -= insets.left + insets.right + 100;
				a.height -= insets.top + insets.bottom;
				for (; i < len; i++) {
					info = highlights[i];
					if (info.getClass().getName()
							.indexOf("LayeredHighlightInfo") > -1) {
						final Highlighter.HighlightPainter p = info
								.getPainter();
						p.paint(g, info.getStartOffset(), info.getEndOffset(),
								a, this.component);
					}
				}
			}
		}
	}
}

/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class TournamentSelectionTest {

	@Test
	public void testTournament() {
		TournamentSelection tournament = new TournamentSelection();
		tournament.setMaximize(true);
		assertTrue(tournament.isMaximize());
	}

}

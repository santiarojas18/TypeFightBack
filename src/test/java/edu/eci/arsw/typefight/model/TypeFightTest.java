package edu.eci.arsw.typefight.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TypeFightTest {

    private TypeFight typeFight;

    @Before
    public void setUp() {
        typeFight = new TypeFight();
    }

    @Test
    public void testAddPlayer() {
        Player player = new Player("TestPlayer", "Red");
        typeFight.addPlayer(player);

        assertTrue(typeFight.getPlayers().contains(player));
        assertEquals(1, typeFight.getAmountOfPlayers());
    }

    @Test
    public void testAddPointToPlayer() {
        Player player = new Player("TestPlayer", "Red");
        typeFight.addPlayer(player);

        typeFight.addPointToPlayer("Red", "Word");
        assertEquals(4, player.getPoints().intValue());
    }

    @Test
    public void testDoDamage() {
        Player player = new Player("TestPlayer", "Red");
        typeFight.addPlayer(player);

        typeFight.doDamage("Red", "DamageWord");
        assertEquals(90, player.getHealth().intValue());
    }

    @Test
    public void testIsThereAWinner() {
        Player player = new Player("TestPlayer", "Red");
        typeFight.addPlayer(player);

        Player winner = typeFight.isThereAWinner();
        assertEquals(player,winner);

        typeFight.doDamage("Red", "DamageWord");
        winner = typeFight.isThereAWinner();
        assertSame(player, winner);
    }

    @Test
    public void testGetSortedPlayers() {
        Player player1 = new Player("Player1", "Red");
        Player player2 = new Player("Player2", "Blue");

        typeFight.addPlayer(player1);
        typeFight.addPlayer(player2);

        typeFight.addPointToPlayer("Red", "Word1");
        typeFight.addPointToPlayer("Blue", "Word2");

        List<Player> sortedPlayers = typeFight.getSortedPlayers();

        assertEquals(2, sortedPlayers.size());
        assertSame(player1, sortedPlayers.get(0));
        assertSame(player2, sortedPlayers.get(1));
    }

    @Test
    public void testAddRandomWord() {
        String word = "NewWord";
        typeFight.addRandomWord(word);

        assertTrue(typeFight.getCurrentWords().contains(word));
    }
}

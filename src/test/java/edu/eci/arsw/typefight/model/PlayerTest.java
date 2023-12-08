package edu.eci.arsw.typefight.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class PlayerTest {

    private Player player;
    private Player player2;

    @Before
    public void setUp() {
        player = new Player("TestPlayer", "Red");
        player2 = new Player("TestPlayer2", "Red", 100, 100, true);
    }

    @Test
    public void testGetPlayersName() {
        assertEquals("TestPlayer", player.getName());
        assertEquals("TestPlayer2", player2.getName());
    }

    @Test
    public void testGetPlayersColor() {
        assertEquals("Red", player.getColor());
        assertEquals("Red", player2.getColor());
    }

    @Test
    public void testAddPoints() {
        player.addPoints(50);
        assertEquals(50, player.getPoints().intValue());
    }

    @Test
    public void testDecreaseHealth() {
        player.decreaseHealth(30);
        assertEquals(70, player.getHealth().intValue());
    }

    @Test
    public void testDecreaseHealth_PlayerDies() {
        player.decreaseHealth(100);
        assertFalse(player.isAlive());
        assertEquals(0, player.getHealth().intValue());
    }

    @Test
    public void testToString() {
        String expectedToString = "Player{name='TestPlayer', color='Red', health=100, points=0}";
        assertEquals(expectedToString, player.toString());
    }

    @Test
    public void testSetPlayerName() {
        assertEquals("TestPlayer", player.getName());
        player.setName("NewTestPlayerName");
        assertEquals("NewTestPlayerName", player.getName());
    }

    @Test
    public void testSetPlayerColor() {
        assertEquals("Red", player.getColor());
        player.setColor("Green");
        assertEquals("Green", player.getColor());
    }
}

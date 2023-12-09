package edu.eci.arsw.typefight;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class WebsocketsMsbrokerExampleApplicationTests {

	private Player player;
    private TypeFight typeFight;

	@Before
    public void setUp() {
        player = new Player("John", "Rojo");
        typeFight = new TypeFight();
    }

    @Test
    public void testAddPlayer() {
        typeFight.addPlayer(player);
        List<Player> players = typeFight.getSortedPlayers();
        List<Player> actualPlayers = new ArrayList<>();
        actualPlayers.add(player);
        assertEquals(players, actualPlayers);
    }


}

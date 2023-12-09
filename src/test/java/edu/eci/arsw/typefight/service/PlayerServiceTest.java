package edu.eci.arsw.typefight.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.repository.PlayerRepository;

public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddPlayer() {
        Player playerToAdd = new Player("John", "Red");
        when(playerRepository.save(any(Player.class))).thenReturn(playerToAdd);
        Player result = playerService.addPlayer(playerToAdd);
        verify(playerRepository, times(1)).save(any(Player.class)); // Verificar que el método save fue llamado exactamente una vez
        assertEquals("John", result.getName());
        assertEquals("Red", result.getColor());
    }

    @Test
    public void testGetPlayerById() {
        Player playerToRetrieve = new Player("Alice", "Blue");
        when(playerRepository.findOne("Alice")).thenReturn(playerToRetrieve);
        Player result = playerService.getPlayerById("Alice");
        verify(playerRepository, times(1)).findOne("Alice"); // Verificar que el método findOne fue llamado exactamente una vez
        assertEquals("Alice", result.getName());
        assertEquals("Blue", result.getColor());
    }
}

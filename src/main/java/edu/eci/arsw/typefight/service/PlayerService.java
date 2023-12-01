package edu.eci.arsw.typefight.service;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public Player addPlayer (Player player) {
        return playerRepository.save(player);
    }

    public Player getPlayerById (String name) {
        return playerRepository.findOne(name);
    }
}

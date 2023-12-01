package edu.eci.arsw.typefight.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import edu.eci.arsw.typefight.model.Player;

@Repository
public interface  PlayerRepository extends CrudRepository<Player, String> {

}

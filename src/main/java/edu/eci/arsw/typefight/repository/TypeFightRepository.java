package edu.eci.arsw.typefight.repository;

import edu.eci.arsw.typefight.model.TypeFight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeFightRepository extends CrudRepository<TypeFight, Integer> {
}

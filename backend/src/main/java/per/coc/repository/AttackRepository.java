package per.coc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import per.coc.entity.Attack;

public interface AttackRepository extends JpaRepository<Attack, Integer> {

    boolean existsByAttackerTagAndWarTagAndDefenderTag(String attackerTag, String warTag, String defenderTag);

}

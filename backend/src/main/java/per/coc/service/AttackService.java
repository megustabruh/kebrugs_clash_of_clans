package per.coc.service;

import per.coc.entity.Attack;
import java.util.List;

public interface AttackService {

    Attack saveAttack(Attack attack);

    List<Attack> getAllAttacks();
    
    boolean attackExists(String attackerTag, String warTag, String defenderTag);
    
}

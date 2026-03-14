package per.coc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import per.coc.entity.Attack;
import per.coc.repository.AttackRepository;

import java.util.List;

@Service
public class AttackServiceImpl implements AttackService {

    @Autowired
    private AttackRepository attackRepository;

    @Override
    public Attack saveAttack(Attack attack) {
        return attackRepository.save(attack);
    }

    @Override
    public List<Attack> getAllAttacks() {
        return attackRepository.findAll();
    }

    @Override
    public boolean attackExists(String attackerTag, String warTag, String defenderTag) {
        return attackRepository.existsByAttackerTagAndWarTagAndDefenderTag(attackerTag, warTag, defenderTag);
    }
}

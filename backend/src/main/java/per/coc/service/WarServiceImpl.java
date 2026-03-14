package per.coc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import per.coc.entity.War;
import per.coc.repository.WarRepository;

import java.util.List;

@Service
public class WarServiceImpl implements WarService {

    @Autowired
    private WarRepository warRepository;

    @Override
    public War saveWar(War war) {
        return warRepository.save(war);
    }

    @Override
    public List<War> getAllWars() {
        return warRepository.findAll();
    }

    @Override
    public boolean warExists(String warTag) {
        return warRepository.existsById(warTag);
    }
}

package per.coc.service;

import per.coc.entity.War;
import java.util.List;

public interface WarService {
    War saveWar(War war);
    List<War> getAllWars();
    boolean warExists(String warTag);
}

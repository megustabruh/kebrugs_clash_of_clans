package per.coc.service;

import java.util.List;
import java.util.Optional;

import per.coc.entity.Player;

public interface PlayerService {
    Player savePlayer(Player player);

    Optional<Player> getPlayerByTag(String tag);

    List<Player> getAllPlayers();

    Player updatePlayer(Player player);

    void deletePlayer(String tag);

    boolean playerExists(String tag);
}

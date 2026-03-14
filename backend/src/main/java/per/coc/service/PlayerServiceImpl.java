package per.coc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import per.coc.entity.Player;
import per.coc.repository.PlayerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Optional<Player> getPlayerByTag(String tag) {
        return playerRepository.findById(tag);
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Player updatePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(String tag) {
        playerRepository.deleteById(tag);
    }

    @Override
    public boolean playerExists(String tag) {
        return playerRepository.existsById(tag);
    }

}

package per.coc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import per.coc.entity.Player;
import per.coc.service.PlayerService;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return ResponseEntity.ok(playerService.savePlayer(player));
    }

    @GetMapping("/{tag}")
    public ResponseEntity<Player> getPlayer(@PathVariable String tag) {
        return playerService.getPlayerByTag(tag)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @PutMapping("/{tag}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String tag, @RequestBody Player player) {
        player.setTag(tag);
        return ResponseEntity.ok(playerService.updatePlayer(player));
    }

    @DeleteMapping("/{tag}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String tag) {
        playerService.deletePlayer(tag);
        return ResponseEntity.noContent().build();
    }

}

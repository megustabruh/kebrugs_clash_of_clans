package per.coc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import per.coc.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, String> {

}

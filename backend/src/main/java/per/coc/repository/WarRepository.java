package per.coc.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import per.coc.entity.War;

public interface WarRepository extends JpaRepository<War, String> {
}


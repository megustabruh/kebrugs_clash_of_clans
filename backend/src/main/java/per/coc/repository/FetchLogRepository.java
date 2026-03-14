package per.coc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import per.coc.entity.FetchLog;

import java.util.List;

public interface FetchLogRepository extends JpaRepository<FetchLog, Long> {
    List<FetchLog> findTop50ByOrderByRunTimeDesc();
}

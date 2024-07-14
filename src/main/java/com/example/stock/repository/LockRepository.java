package com.example.stock.repository;

import com.example.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 편의를 위해 Stock entity 를 사용
public interface LockRepository extends JpaRepository<Stock, Long> {
    // ※ 주의
    //해당 예제는 편의성을 위해 Jpa의 Native Query 기능을 활용하여 구현하여 동일한 데이터 소스 사용
    // 하지만 실무에서 사용할 때는 data source를 분리해서 사용을 추천.
    // 이유: 같은 데이터 소스를 사용하면, 커넥션 풀이 부족해지는 현상으로 다른 서비스에도 영향을 끼칠 수 있다.
    // 실무에서는 별도의 JDBC를 사용하거나 해야함!!

    //get lock 명령어와 release lock 명령어 작성
    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(@Param("key") String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(@Param("key") String key);
    //실제 로직 전후로 락 획득, 락 해제를 해줘야하기 때문에 Facade class를 추가하자.
}

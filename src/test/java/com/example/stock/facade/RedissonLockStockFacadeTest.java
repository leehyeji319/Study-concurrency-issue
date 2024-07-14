package com.example.stock.facade;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonLockStockFacadeTest {

    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 동시에_100개_요청() throws InterruptedException {
        int threadCount = 100;
        // 멀티 스레드를 위해 ExecutorService 사용 - 비동기
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        //백개 모두 떨어질때 까지 기다려야하므로 CountDownLatch사용
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockStockFacade.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        //CountDownLatch 는 다른 스레드에서 수행중인 작업이 완료될 때까지 대기하도록 하는 클래스

        //모든 요청이 완료된다면
        Stock stock = stockRepository.findById(1L).orElseThrow();
        //예상 - 100개 저장하고 한개씩 감소시키기 때문에 0개를 예상
        //100 - (1*100) = 0
        assertEquals(0, stock.getQuantity());
    }

    //※ 만약 테스트 케이스가 실패한다면, lock을 기다려 주는 시간을 늘려준다.
    // boolean available = lock.tryLock(15, 1, TimeUnit.SECONDS);

    //redisson은 pub-sub 기반이기 때문에 redis의 부하가 적다.
    //하지만 구현이 조금 복잡하다는 단점과, 별도의 라이브러리를 사용해야하는 부담감이 있다.

    /**
     *
     * ### lettuce와 redisson 비교
     *
     * - Lettuce
     *     - 구현이 간단하다
     *     - spring data redis를 이용하면 lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다.
     *     - spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있다.
     * - Redisson
     *     - 락 획득 재시도를 기본으로 제공한다.
     *     - pub-sub 방식으로 구현이 되어있기 때문에 lettuce와 비교했을 때 redis에 부하가 덜 간다.
     *     - 별도의 라이브러리를 사용해야 한다.
     *     - lock을 라이브러리 차원에서 제공해주기 때문에 사용법을 비교해야한다.
     *
     * 실무에서는?
     *
     * - 재시도가 필요하지 않은 lock은 lettuce를 활용
     * - 재시도가 필요한 경우에는 redisson 활용
     *
     *
     *
     * ### MySQL과 Redis 비교
     *
     *
     * - MySQL
     *     - 이미 Mysql을 사용하고 있따면 별도의 비용없이 사용가능하다.
     *     - 어느정도의 트래픽까지는 문제없이 활용이 가능하다.
     *     - Redis보다는 성능이 좋지 않다.
     *
     *
     * - Redis
     *     - 활용중인 Redis가 없다면 별도의 구축비용과 인프라 관리비용이 발생한다.
     *     - Mysql보다 성능이 좋다.
     *
     * => 실무에서는 비용적 여유가 없거나 MySQL로 처리가 가능할 정도의 트래픽이라면 mysql활용하고,
     *    비용적 여유가 있거나 MySQL로 처리가 불가능할 정도의 트래픽이라면 Redis를 도입
     */
}
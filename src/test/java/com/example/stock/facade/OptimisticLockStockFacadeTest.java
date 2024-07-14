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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OptimisticLockStockFacadeTest {

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

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
                    optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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

    // optimisticLock의 장점 1. 별도의 락을 잡지 않으므로 pessimistic lock보다 성능상 이점이 있다.
    // 단점: 1. 업데이트가 실패했을 때 개발자가 직접 재시도 로직을 작성해줘야함
    // 2. 충돌이 빈번하게 일어나거나 예상된다면 pessimistic을 추천. 빈번하지 않다면 optimistic lock을 추천한다.
}
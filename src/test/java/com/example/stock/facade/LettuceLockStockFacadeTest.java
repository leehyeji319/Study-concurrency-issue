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
class LettuceLockStockFacadeTest {


    @Autowired
    private LettuceLockStockFacade lettuceLockStockFacade;

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
                    lettuceLockStockFacade.decrease(1L, 1L);
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

    // lettuce를 활용한 방법의 장점
    // 1. 구현이 비교적 간단하다.
    // 단점
    // 1. spin lock 방식이므로 redis에 부하를 줄 수 있다
    // -> 따라서 해결을 위해 우리가 했던 것처럼 thread sleep을 통해 lock 획득 재시도 할 때 텀을 둬서 방지
}
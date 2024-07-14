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
class NamedLockStockFacadeTest {

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

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
                    namedLockStockFacade.decrease(1L, 1L);
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

    // 날라가는 sql query 문 확인
    // select get_lock(?, 3000)
    // select release_lock(?)
    // ※ 설명
    // named lock은 주로 분산락을 구현할 때 사용한다.
    // 그리고 Pessimistic Lock은 타임아웃을 구현하기 힘들지만, Named Lock은 타임아웃을 손쉽게 구현 가능하다.
    // 이외에도 데이터 삽입시에 정합성을 맞춰야하는 경우에도 Named Lock 사용.
    // 하지만 이방법은 transaction 종료 시에 락 해제, 세션 관리를 잘 해줘야하기 때문에 주의해서 사용해야함
    // 실제로 사용할 때는 구현 방법이 복잡함 ...
}
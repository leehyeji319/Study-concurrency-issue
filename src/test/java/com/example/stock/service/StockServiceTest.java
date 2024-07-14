package com.example.stock.service;

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
class StockServiceTest {


    @Autowired
    private PessimisticLockStockService stockService;
//    private StockService stockService;

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
    public void 재고감소() {
        stockService.decrease(1L, 1L);

        // 100 - 1 = 99 개가 남아있어야한다
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
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
                    stockService.decrease(1L, 1L);
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
        //왜 96개 일까 ? -> 레이스 컨디션이 일어났기 때문이다.
        // race condition : 둘 이상의 스레드가 공유데이터에 액세스할 수 있고. 동시에 변경을 하려고 할 때 발생하는 문제.
        // 해결 방안 : 하나의 스레드가 작업이 완료된 이후에 다른 스레드가 접근 할 수 있도록 변경을 해주면 된다.
    }

    //1. service decrease에 synchronized를 활용하여 붙인다.  -> 실패 43이 나왔다.
    //이유: 스프링의 @Transactional 어노테이션 동작 방식때문이다.
    // @Transactional을 이용하면 우리가 만든 클래스를 wrapping하여 새로 만들어서 실행한다. proxy ? (ex. TransactionStockService)
    // Transaction을 시작하고 메소드 호출 후 메소드 실행이 종료된다면 Transaction을 종료한다. endTransaction()
    // Transaction 종료를 하는 시점에 DB에 업데이트를 하는데, decrease 메서드가 완료가 되었고 실제 DB가 업데이트 되기 이전에 다른 스레드가 decrease를 호출
    // 그러면 다른 스레드는 갱신 이전의 값을 가져가서 이전과 동일한 문제가 발생하게 된다. 여기서는 @Transactional을 주석처리하면 일단 해결된다.

    //1-1. java의 synchronized를 이용했을 때 발생할 수 있는 문제.
    //java의 synchronized는 하나의 프로세스 안에서만 보장이 된다. 서버가 한대면 괜찮겠지만, 여러대라면..
    //결국 여러 스레드에서 동시에 데이터에 접근을 할 수 있게 되므로 race condition 발생한다.
    //실제 운영중인 서비스는 두대 이상의 서버를 사용하기 때문에 synchronized를 사용하지 않는다.. (-> mysql에서 지원해주는 해결해보자)

}
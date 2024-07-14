package com.example.stock.facade;

import com.example.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockStockFacade {

    //lock 획득에 사용할 레디슨 클라이언트 필드 추가, 재고 감소를 위한 stockService 추가
    private final RedissonClient redissonClient;
    private final StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        //redisson client 를 사용해서 lock 객체를 가져온다.
        RLock lock = redissonClient.getLock(id.toString());

        try {
            // 후에 몇 초동안 락 획득을 시도할 것인지,
            // 몇 초 동안 점유할 것인지 설정한 후 락을 획득한다.
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            //만약 락 획득을 실패하였다면 로그 남긴 후, return
            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }
            //lock 획득을 성공했다면 재고 감소
            stockService.decrease(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //그리고 로직이 정상 종료가 되었다면 락을해제한다.
            lock.unlock();
        }
    }
}

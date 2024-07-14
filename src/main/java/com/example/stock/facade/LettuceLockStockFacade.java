package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    //redis를 활용해서 lock을 수행할 수 있어야하기 때문에 redis lock 레포지토리, 재고감소를 위한 stockService추가
    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {
            //만약에 락 획득 실패했다면 thread 사용해서 100ms 후에 재시도 하도록 설정
            //이렇게 해야 레디스에 가는 부하를 줄여줄 수 있다.
            Thread.sleep(100);
        }

        try {
            //락 획득에 성공했다면, stock service를 활용하여 재고감소
            stockService.decrease(id, quantity);
        } finally {
            //로직이 모두 종료되었다면 unlock메서드로 락을 해제
            redisLockRepository.unlock(id);
        }
    }
}

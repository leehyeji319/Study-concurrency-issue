package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        stock.decrease(quantity);
        stockRepository.save(stock);
        //optimistic은 실패했을 때 재시도를 해야하므로, facade라는 패키지를 만들고 재시도 로직을 짜줘야한다.
    }

    // optimisticLock의 장점 1. 별도의 락을 잡지 않으므로 pessimistic lock보다 성능상 이점이 있다.
    // 단점: 1. 업데이트가 실패했을 때 개발자가 직접 재시도 로직을 작성해줘야함
    // 2. 충돌이 빈번하게 일어나거나 예상된다면 pessimistic을 추천. 빈번하지 않다면 optimistic lock을 추천한다.
}

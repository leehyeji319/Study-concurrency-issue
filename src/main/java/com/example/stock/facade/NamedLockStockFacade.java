package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            //락 획득하기
            lockRepository.getLock(id.toString());
            //락 획득 후 재고 감소
            stockService.decrease(id, quantity);
        } finally {
            //모든 로직이 종료 되었을 때 락 해제하기
            lockRepository.releaseLock(id.toString());
        }
    }
}

package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        //update를 실패했을 때 재시도를 해야하므로 while문 사용

        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);

                //정상적으로 실행이 되었다면 break문으로 빠져나오기
                break;
            } catch (Exception e) {
                //수량 감소에 실패했다면, 50밀리초 기다리고 재시도
                Thread.sleep(50);
            }
        }
    }
}

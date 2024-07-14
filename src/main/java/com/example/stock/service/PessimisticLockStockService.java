package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessimisticLockStockService {
    //데이터베이스에 락을 거는 PessimisticLock

    private final StockRepository stockRepository;

    public PessimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);

        stockRepository.save(stock);
    }
    //query를 확인하면  select s1_0.id,s1_0.product_id,s1_0.quantity from stock s1_0 where s1_0.id=? for update 가 나오는데
    //여기서 for update가 락을 걸고 데이터를 가져오는 부분이다
    //장점: 1.충돌이 빈번하게 일어난다면 optimistic lcok보다 성능이 좋다
    //2. 락을 통해 업데이트를 제어하기 때문에 데이터 정합성이 보장된다.
    //단점: 별도의 락을 잡기때문에 성능 감소가 있을 수 있다.


    //cf. optimistic lock: 실제로 락을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법
}

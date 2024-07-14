package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    //재고 감소
    // java에서는 synchronized를 활용하면 손쉽게 한개의 스레드에만 접근 가능하게 할 수 있다.
    // synchronized를 붙여도 실패. 왜일까? 이유는 @Transactional의 동작 방식때문이다.
    // @Transactional을 이용하면 우리가 만든 클래스를 wrapping하여 새로 만들어서 실행한다. proxy ? (ex. TransactionStockService)
    // Transaction을 시작하고 메소드 호출 후 메소드 실행이 종료된다면 Transaction을 종료한다. endTransaction()
    // Transaction 종료를 하는 시점에 DB에 업데이트를 하는데, decrease 메서드가 완료가 되었고 실제 DB가 업데이트 되기 이전에 다른 스레드가 decrease를 호출
    // 그러면 다른 스레드는 갱신 이전의 값을 가져가서 이전과 동일한 문제가 발생하게 된다. 여기서는 @Transactional을 주석처리하면 일단 해결된다.
//    @Transactional  // 해당 동작 방식을 잠깐 주석
    /*
    public synchronized void decrease(Long id, Long quantity) {    // @Transactional 애노테이션을 붙이고 synchronized를 붙였는 데 testcase 실패. 왜? 위에 설명
        // Stock 조회
        // 재고 감소 뒤
        // 갱신된 값을 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        //saveAndFlush
        stockRepository.saveAndFlush(stock);
    }
    */


    // db 예제 3번. namedlock 사용할 때
    // 부모의 transaction과 별도로 실행해줘야하기 때문에 propagation 변경해주자
    //TODO: propagation이 뭔지?

    // ※ 같은 데이터 소스를 사용하기 때문에 커넥션 풀 사이즈를 변경해주자.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소 뒤
        // 갱신된 값을 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        //saveAndFlush
        stockRepository.saveAndFlush(stock);
    }
}

package com.example.stock.transaction;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Service;

@Service
public class TransactionStockService {

    private StockService stockService;

    public TransactionStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long qauntity) {
        startTransaction();

        stockService.decrease(id, qauntity);

        endTransaction();
    }

    private void startTransaction() {
        System.out.println("Transaction started");
    }

    private void endTransaction() {
        System.out.println("Commit");
    }

}

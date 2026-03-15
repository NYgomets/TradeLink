package site.tradelink.tradelink.stock.repository;

import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;

import java.util.List;

public interface CurrentExchangeRateCustomRepository {
    List<CurrentExchangeRate> findAll();
}

package site.tradelink.tradelink.exchangeRate.repository;

import site.tradelink.tradelink.exchangeRate.entity.CurrentExchangeRate;

import java.util.List;

public interface CurrentExchangeRateCustomRepository {
    List<CurrentExchangeRate> findAll();
}

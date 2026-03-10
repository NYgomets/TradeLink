package site.tradelink.tradelink.stock.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CurrentExchangeRateCustomRepositoryImpl implements  CurrentExchangeRateCustomRepository{

    private final EntityManager em;

    @Override
    public List<CurrentExchangeRate> findAll() {
        return em.createQuery(
                "SELECT c FROM CurrentExchangeRate c", CurrentExchangeRate.class
        ).getResultList();
    }
}

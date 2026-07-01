package site.tradelink.tradelink.cryptocurrency.disruptor;

import com.lmax.disruptor.EventFactory;

public class OrderEventModelFactory implements EventFactory<OrderEventModel> {
    @Override
    public OrderEventModel newInstance() {
        return new OrderEventModel();
    }
}

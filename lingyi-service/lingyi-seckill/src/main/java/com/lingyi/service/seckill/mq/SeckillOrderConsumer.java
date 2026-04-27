package com.lingyi.service.seckill.mq;

import com.lingyi.service.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lingyi.seckill", name = "async-mode", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "${lingyi.seckill.topic:lingyi-seckill-create-order}",
        consumerGroup = "${lingyi.seckill.consumer-group:lingyi-seckill-consumer}"
)
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {

    private final SeckillService seckillService;

    @Override
    public void onMessage(SeckillOrderMessage message) {
        seckillService.processOrderMessage(message);
    }
}

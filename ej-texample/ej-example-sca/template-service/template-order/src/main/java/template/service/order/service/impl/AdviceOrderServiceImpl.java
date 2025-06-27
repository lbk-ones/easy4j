package template.service.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import template.service.order.domains.AdviceOrder;
import template.service.order.mapper.AdviceOrderMapper;
import template.service.order.service.AdviceOrderService;

@Service
public class AdviceOrderServiceImpl extends ServiceImpl<AdviceOrderMapper, AdviceOrder> implements AdviceOrderService {

}
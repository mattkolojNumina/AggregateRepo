package records;

import java.util.List;

public record GeekPutawayOrder(
        String warehouseCode,
        String receiptCode,
        String palletCode,
        int type,
        List<GeekPutawayOrderSku> skus) {}

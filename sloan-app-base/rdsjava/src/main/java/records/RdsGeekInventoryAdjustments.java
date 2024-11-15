package records;

import java.time.Instant;

public class RdsGeekInventoryAdjustments {
    private int seq;
    private final String transaction_type;
    private final String putaway_type;
    private final String putaway_replen_code;
    private final String putaway_code;
    private final String part_number;
    private final int quantity_change;
    private final String operator_id;
    private final String trackingNumber;
    
    public int getSeq() {
        return seq;
    }
    public String getTransaction_type() {
        return transaction_type;
    }
    public String getPart_number() {
        return part_number;
    }
    public String getPutaway_replen_code() {
        return putaway_replen_code;
    }
    public String getPutaway_code() {
        return putaway_code;
    }
    public String getPutaway_type() {
        return putaway_type;
    }
    public int getQuantity_change() {
        return quantity_change;
    }
    public String getOperator_id() {
        return operator_id;
    }
    public String getTrackingnumber() {
        return trackingNumber;
    }

    private RdsGeekInventoryAdjustments(RdsGeekInventoryAdjustmentsBuilder builder) {
        this.transaction_type = builder.transaction_type;
        this.part_number = builder.part_number;
        this.putaway_replen_code = builder.putaway_replen_code;
        this.putaway_code = builder.putaway_code;
        this.putaway_type = builder.putaway_type;
        this.quantity_change = builder.quantity_change;
        this.operator_id = builder.operator_id;
        this.trackingNumber = builder.trackingNumber;
    }

    public static class RdsGeekInventoryAdjustmentsBuilder {
        private final String transaction_type;
        private final String part_number;
        private final String putaway_replen_code;
        private final String putaway_code;
        private final String putaway_type;
        private final int quantity_change;
        private final String operator_id;
        private final String trackingNumber;

        public RdsGeekInventoryAdjustmentsBuilder(
            String transaction_type,
            String part_number, 
            String putaway_replen_code,
            String putaway_code,
            String putaway_type,
            int quantity_change, 
            String operator_id,
            String trackingNumber) {
            this.transaction_type = transaction_type;
            this.part_number = part_number;
            this.putaway_replen_code = putaway_replen_code;
            this.putaway_code = putaway_code;
            this.putaway_type = putaway_type;
            this.quantity_change = quantity_change;
            this.operator_id = operator_id;
            this.trackingNumber = trackingNumber;
        }

        public RdsGeekInventoryAdjustments build(){
            return new RdsGeekInventoryAdjustments(this);
        }
    }


}

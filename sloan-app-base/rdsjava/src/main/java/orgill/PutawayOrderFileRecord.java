package sloane;

import dao.CustPutawayOrderDAO;
import dao.DataAccessException;
import host.FileRecord;
import records.CustPutawayOrder;

import java.util.ArrayList;
import java.util.List;

public class PutawayOrderFileRecord implements FileRecord {

    private final String palletCode;
    private final String putawayOrderCodeReplenishment;
    private final String putawayOrderCodeReceiving;
    private final int putawayType;
    private final String productPartNumber;
    private final String buyingDepartmentCode;
    private final int quantity;
    private final String uom;
    private final int shelfQuantity;

    private final int NORMAL_RECEIVING_PUTAWAY_TYPE = 1;
    private final int RETURNS_RECEIVING_PUTAWAY_TYPE = 4;
    private final int REPLENISHMENT_PUTAWAY_TYPE = 5;

    public PutawayOrderFileRecord(String[] fields) {
        String palletCodeTrimmed = fields[0].trim();
        if (palletCodeTrimmed.equals("00000000")) {
            palletCode = null;
        } else {
            palletCode = palletCodeTrimmed;
        }
        putawayOrderCodeReplenishment = fields[1].trim();
        putawayOrderCodeReceiving = fields[2].trim();
        putawayType = Integer.parseInt(fields[3].trim());
        productPartNumber = fields[4].trim();
        buyingDepartmentCode = fields[5].trim();
        quantity = Integer.parseInt(fields[6].trim());
        uom = fields[7].trim();
        shelfQuantity = Integer.parseInt(fields[8].trim());
    }

    @Override
    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();

        if (putawayType == NORMAL_RECEIVING_PUTAWAY_TYPE || putawayType == RETURNS_RECEIVING_PUTAWAY_TYPE) {
            if (putawayOrderCodeReceiving.isBlank()) {
                validationErrors.add("Field [Putaway Order Code Receiving] is empty.");
            }
        } else if (putawayType == REPLENISHMENT_PUTAWAY_TYPE) {
            if (putawayOrderCodeReplenishment.isBlank()) {
                validationErrors.add("Field [Putaway Order Code Replenishment] is empty.");
            }
        } else {
            validationErrors.add(String.format("Field [Putaway Type] contains invalid value: [%d]", putawayType));
        }

        if (productPartNumber.isBlank()) {
            validationErrors.add("Field [Product Part Number] is empty.");
        }
        // lookup SKU

        if (buyingDepartmentCode.isBlank()) {
            validationErrors.add("Field [Buying Department Code] is empty.");
        }
        // lookup buying department code

        if (quantity <= 0) {
            validationErrors.add("Field [Quantity] must be greater than zero.");
        }

        if (uom.isBlank()) {
            validationErrors.add("Field [UOM] is empty.");
        }

        if (shelfQuantity <= 0) {
            validationErrors.add("Field [Shelf Quantity] must be greater than zero.");
        }

        return validationErrors;
    }

    @Override
    public void persist() throws DataAccessException {
        CustPutawayOrderDAO dao = new CustPutawayOrderDAO();
        CustPutawayOrder order = new CustPutawayOrder.CustPutawayOrderBuilder(getPutawayOrderCode())
                .setPalletCode(this.palletCode)
                .setPutawayType(this.putawayType)
                .setSku(this.productPartNumber)
                .setUom(this.uom)
                .setBuyingDepartmentCode(this.buyingDepartmentCode)
                .setQuantity(this.quantity)
                .setShelfQuantity(this.shelfQuantity)
                .build();
        dao.save(order);
    }

    public String getPutawayOrderCode() {
        if (this.putawayType == 5) {
            return this.putawayOrderCodeReplenishment;
        }
        return "0"+this.putawayOrderCodeReceiving; // SR: bugfix/289
    }
}

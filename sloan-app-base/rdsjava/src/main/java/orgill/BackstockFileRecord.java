package sloane;

import dao.DataAccessException;
import dao.CustBackstockInventoryDAO;
import dao.SloaneCommonDAO;
import dao.ProOperatorDAO;
import host.FileRecord;
import records.CustBackstockInventory;
import records.ProOperator;

import java.util.ArrayList;
import java.util.List;

import static host.StringUtils.isNumeric;

public class BackstockFileRecord implements FileRecord {

    private final String location;
    private final String sku;
    private final String qty;


    public BackstockFileRecord(String[] fields) {
        sku = fields[0].trim();
        location = fields[1].trim();
        qty = fields[2].trim();
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();

        if (sku == null || sku.isBlank()) {
            validationErrors.add("Field [sku] is empty.");
        } else if (sku.length() > 7) {
            validationErrors.add("Field [sku] length exceeds 7 characters.");
        }

        if (location == null || location.isBlank()) {
           validationErrors.add("Field [location] is empty.");
        } else if (location.length() > 7) {
      	  validationErrors.add("Field [location] length exceeds 7 characters.");
        }
        
        if (qty == null || qty.isBlank() ) {
           validationErrors.add("Field [qty] is empty.");
        } else if (qty.length() > 7) {
      	  validationErrors.add("Field [qty] length exceeds 7 characters.");
        }else if (!isNumeric(qty) ) {
            validationErrors.add("Field [qty] is not numeric.");
        }

        return validationErrors;
    }

    public void persist() throws DataAccessException {
        CustBackstockInventoryDAO dao = new CustBackstockInventoryDAO();

        if(dao.getNoOfOldRecords() > 0){    // this should only run once at the start when 1 good record is found
            dao.copyOverAndTruncate();
        }

        CustBackstockInventory custBackstockInventory = new CustBackstockInventory.CustBackstockInventoryBuilder(
                location,
                sku,
                qty
        ).build();
        dao.save(custBackstockInventory);
    }
}

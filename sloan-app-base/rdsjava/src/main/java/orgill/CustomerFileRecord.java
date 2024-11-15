package sloane;

import dao.DataAccessException;
import dao.CustCustomerDAO;
import host.FileRecord;
import records.CustCustomer;

import java.util.ArrayList;
import java.util.List;

public class CustomerFileRecord implements FileRecord {

    private final String customerNumber;
    private final String customerName;
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String city;
    private final String state;
    private final String zipcode;
    private final String exportFlag;
    private static final String Y = "Y";
    private static final String N = "N";

    public CustomerFileRecord(String[] fields) {
   	 customerNumber = fields[0].trim();
   	 customerName = fields[1].trim();
   	 addressLine1 = fields[2].trim();
   	 addressLine2 = fields[3].trim();
   	 addressLine3 = fields[4].trim();
   	 city = fields[5].trim();
   	 state = fields[6].trim();
   	 zipcode = fields[7].trim();
   	 exportFlag = fields[8].trim();
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (customerNumber == null || customerNumber.isBlank()) {
           validationErrors.add("Field [CustomerNumber] is empty.");
        } else if (customerNumber.length() > 25) {
      	  validationErrors.add("Field [CustomerNumber] length exceeds 25 characters.");
        }
        
        if (customerName == null || customerName.isBlank()) {
           validationErrors.add("Field [CustomerName] is empty.");
        } else if (customerName.length() > 25) {
      	  validationErrors.add("Field [CustomerName] length exceeds 25 characters.");
        }
        
        if (addressLine1 != null && addressLine1.length() > 35) {
      	  validationErrors.add("Field [AddressLine1] length exceeds 35 characters.");
        }
        
        if (addressLine2 != null && addressLine2.length() > 35) {
      	  validationErrors.add("Field [AddressLine2] length exceeds 35 characters.");
        }
        
        if (addressLine3 != null && addressLine3.length() > 35) {
      	  validationErrors.add("Field [AddressLine1] length exceeds 35 characters.");
        }
        
        if ( (addressLine1 == null || addressLine1.isBlank()) && 
      		 (addressLine2 == null || addressLine2.isBlank()) && 
      		 (addressLine3 == null || addressLine3.isBlank()) ) {
      	  validationErrors.add("Field [AddressLine1,AddressLine2,AddressLine3] are empty.");
        }
        
        if (city == null || city.isBlank()) {
           validationErrors.add("Field [City] is empty.");
        } else if (city.length() > 20) {
      	  validationErrors.add("Field [City] length exceeds 20 characters.");
        }  
        
        if (state == null) {
           validationErrors.add("Field [State] is empty.");
        } else if (state.length() > 2) {
      	  validationErrors.add("Field [State] length exceeds 2 characters.");
        }  
        
        if (zipcode == null) {
           validationErrors.add("Field [Zipcode] is empty.");
        } else if (zipcode.length() > 10) {
      	  validationErrors.add("Field [zipcode] length exceeds 10 characters.");
        }

        if (exportFlag == null) {
            validationErrors.add("Field [ExportFlag] is empty.");
        } else if (exportFlag.length() > 1) {
            validationErrors.add("Field [exportFlag] exceeds 1 character");
        }

        return validationErrors;
    }

    public void persist() throws DataAccessException {
        CustCustomer customer = new CustCustomer(
                customerNumber,
                customerName,
                addressLine1,
                addressLine2,
                addressLine3,
                city,
                state,
                zipcode,
                exportFlag);
        CustCustomerDAO dao = new CustCustomerDAO();
        dao.save(customer);
    }
}

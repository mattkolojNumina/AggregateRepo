package sloane;

import dao.DataAccessException;
import dao.RdsLocationDAO;
import host.FileRecord;
import records.RdsLocation;

import java.util.ArrayList;
import java.util.List;

public class LocationFileRecord implements FileRecord {

    private final String location;
    private final String alias;
    private final String barcode;
    private final String checkDigits;
    private final String department;
    private final String aisle;
    private final String bay;
    private final String slot;
    private final String shelf;
    private final String neighborhood;

    public LocationFileRecord(String[] fields) {
   	 department = fields[0].trim();
   	 aisle = fields[1].trim();
   	 bay = fields[2].trim();
   	 slot = fields[3].trim();
   	 shelf = fields[4].trim();
   	 checkDigits = fields[5].trim();
   	 location = String.format("%s%s%s%s%s", department, aisle, bay, slot, shelf);
   	 barcode = location;
   	 alias = String.format("%s-%s-%s-%s-%s", department, aisle, bay, slot, shelf);
   	 neighborhood = String.format("%s%s", department,aisle);
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (department == null || department.isBlank()) {
           validationErrors.add("Field [department] is empty.");
        } else if (department.length() > 1) {
      	  validationErrors.add("Field [department] length exceeds 1 characters.");
        }
        
        if (aisle == null || aisle.isBlank()) {
           validationErrors.add("Field [aisle] is empty.");
        } else if (aisle.length() != 2) {
      	  validationErrors.add("Field [aisle] length is not 2 characters.");
        }
        
        if (bay == null || bay.isBlank()) {
           validationErrors.add("Field [bay] is empty.");
        } else if (bay.length() != 2) {
      	  validationErrors.add("Field [bay] length is not 2 characters.");
        }
        
        if (slot == null || slot.isBlank()) {
           validationErrors.add("Field [slot] is empty.");
        } else if (slot.length() > 1) {
      	  validationErrors.add("Field [slot] length exceeds 1 characters.");
        }
        
        if (shelf == null || shelf.isBlank()) {
           validationErrors.add("Field [shelf] is empty.");
        } else if (shelf.length() > 1) {
      	  validationErrors.add("Field [shelf] length exceeds 1 characters.");
        }
        
        if (checkDigits == null || checkDigits.isBlank()) {
           validationErrors.add("Field [checkDigits] is empty.");
        } else if (checkDigits.length() != 3) {
      	  validationErrors.add("Field [checkDigits] length is not 3 characters.");
        }

        return validationErrors;
    }

    public void persist() throws DataAccessException {
        RdsLocation loc = new RdsLocation(
                location,
                alias,
                barcode,
                department,
                aisle,
                bay,
                slot,
                shelf,
                neighborhood,
                checkDigits);
        RdsLocationDAO dao = new RdsLocationDAO();
        dao.save(loc);
    }
}

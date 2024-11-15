package sloane;

import dao.DataAccessException;
import dao.CustBuyingDepartmentsDAO;
import host.FileRecord;
import host.StringUtils;
import records.CustBuyingDepartments;

import java.util.ArrayList;
import java.util.List;

public class BuyingDeptFileRecord implements FileRecord {

    private final String departmentName;
    private final String departmentDesc;

    public BuyingDeptFileRecord(String[] fields) {
        departmentName = fields[0].trim();
        departmentDesc = fields[1].trim();
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentDesc() {
        return departmentDesc;
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (getDepartmentName() == null || getDepartmentName().isBlank()) {
            validationErrors.add("Field [departmentName] is empty.");
        } else if (getDepartmentName().length() != 1) {
            validationErrors.add("Field [departmentName] is not 5 characters.");
        }

        if (getDepartmentDesc() == null || getDepartmentDesc().isBlank()) {
            validationErrors.add("Field [departmentDesc] is empty.");
        } else if (getDepartmentDesc().length() > 30) {
            validationErrors.add("Field [departmentDesc] length exceeds 30 characters.");
        }

        return validationErrors;
    }

    public void persist() throws DataAccessException {
        CustBuyingDepartmentsDAO dao = new CustBuyingDepartmentsDAO();
        CustBuyingDepartments buyingDept = new CustBuyingDepartments.CustBuyingDepartmentsBuilder(
                getDepartmentName(),
                getDepartmentDesc()
        ).build();
        dao.save(buyingDept);
    }
}

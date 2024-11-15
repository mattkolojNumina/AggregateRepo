package records;

public class CustBuyingDepartments {
    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentDesc() {
        return departmentDesc;
    }

    private final String departmentName;
    private final String departmentDesc;

    private CustBuyingDepartments(CustBuyingDepartmentsBuilder builder) {
        this.departmentName = builder.departmentName;
        this.departmentDesc = builder.departmentDesc;
    }

    public static class CustBuyingDepartmentsBuilder {
        private final String departmentName;
        private final String departmentDesc;

        public CustBuyingDepartmentsBuilder(String departmentName, String departmentDesc) {
            this.departmentName = departmentName;
            this.departmentDesc = departmentDesc;
        }

        public CustBuyingDepartments build() {
            return new CustBuyingDepartments(this);
        }
    }
}

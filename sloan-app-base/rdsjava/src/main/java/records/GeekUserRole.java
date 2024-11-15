package records;

public class GeekUserRole {

    private final String warehouseCode;
    private final String userName;
    private final String roleName;

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getRoleName() {
        return roleName;
    }

    private GeekUserRole(GeekUserRoleBuilder builder) {
        this.warehouseCode = builder.warehouseCode;
        this.userName = builder.userName;
        this.roleName = builder.roleName;
    }

    public static class GeekUserRoleBuilder {
        private final String warehouseCode;
        private final String userName;
        private final String roleName;


        public GeekUserRoleBuilder(String warehouseCode, String userName, String roleName) {
            this.warehouseCode = warehouseCode;
            this.userName = userName;
            this.roleName = roleName;
            
        }

        public GeekUserRole build() { return new GeekUserRole(this); }
    }
}

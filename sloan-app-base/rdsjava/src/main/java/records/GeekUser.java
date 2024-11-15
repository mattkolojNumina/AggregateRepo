package records;

public class GeekUser {

    private final String warehouseCode;
    private final String userName;
    private final String realName;
    private final String password;
    private final String status;
    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getRealName() {
        return realName;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    private GeekUser(GeekUserBuilder builder) {
        this.warehouseCode = builder.warehouseCode;
        this.userName = builder.userName;
        this.realName = builder.realName;
        this.password = builder.password;
        this.status = builder.status;
    }

    public static class GeekUserBuilder {
        private final String warehouseCode;
        private final String userName;
        private final String realName;
        private final String password;
        private final String status;

        public GeekUserBuilder(String warehouseCode, String userName, String realName, String password, String status) {
            this.warehouseCode = warehouseCode;
            this.userName = userName;
            this.realName = realName;
            this.password = password;
            this.status = status;
        }

        public GeekUser build() { return new GeekUser(this); }
    }
}

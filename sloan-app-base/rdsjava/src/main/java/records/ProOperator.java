package records;

public class ProOperator {
    public String getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    private final String operatorId;
    private final String operatorName;
    private final String password;
    private final String status;

    private ProOperator(ProOperatorBuilder builder) {
        this.operatorId = builder.operatorId;
        this.operatorName = builder.operatorName;
        this.password = builder.password;
        this.status = builder.status;
    }

    public static class ProOperatorBuilder {
        private final String operatorId;
        private final String operatorName;
        private final String password;
        private final String status;

        public ProOperatorBuilder(String operatorId, String operatorName, String password, String status) {
            this.operatorId = operatorId;
            this.operatorName = operatorName;
            this.password = password;
            this.status = status;
        }

        public ProOperator build() {
            return new ProOperator(this);
        }
    }
}

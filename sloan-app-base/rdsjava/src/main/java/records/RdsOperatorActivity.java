package records;


public class RdsOperatorActivity {

  private long seq;
  private String typeCode;
  private String operatorTask;
  private int operatorId;
  private String device;
  private String startStamp;
  private String endStamp;
  private String processed;
  private java.sql.Timestamp createStamp;
  private java.sql.Timestamp stamp;




  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }


  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }


  public String getOperatorTask() {
    return operatorTask;
  }

  public void setOperatorTask(String operatorTask) {
    this.operatorTask = operatorTask;
  }


  public int getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(int operatorId) {
    this.operatorId = operatorId;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }



  public String getStartStamp() {
    return startStamp;
  }

  public void setStartStamp(String startStamp) {
    this.startStamp = startStamp;
  }


  public String getEndStamp() {
    return endStamp;
  }

  public void setEndStamp(String endStamp) {
    this.endStamp = endStamp;
  }


  public String getProcessed() {
    return processed;
  }

  public void setProcessed(String processed) {
    this.processed = processed;
  }


  public java.sql.Timestamp getCreateStamp() {
    return createStamp;
  }

  public void setCreateStamp(java.sql.Timestamp createStamp) {
    this.createStamp = createStamp;
  }


  public java.sql.Timestamp getStamp() {
    return stamp;
  }

  public void setStamp(java.sql.Timestamp stamp) {
    this.stamp = stamp;
  }

  private RdsOperatorActivity(RdsOperatorActivityBuilder builder) {
    this.operatorId = builder.operatorId;
    this.typeCode = builder.typeCode;
    this.operatorTask = builder.operatorTask;
    this.device = builder.device;
    this.startStamp = builder.startStamp;
    this.endStamp = builder.endStamp;
  }

  public static class RdsOperatorActivityBuilder{
    private final String typeCode;
    private final String operatorTask;
    private final int operatorId;
    private final String device;
    private final String startStamp;
    private final String endStamp;
    public RdsOperatorActivityBuilder(
            int operatorId,
            String typeCode,
            String operatorTask,
            String device,
            String startStamp,
            String endStamp) {

      this.typeCode = typeCode;
      this.operatorTask = operatorTask;
      this.operatorId = operatorId;
      this.device = device;
      this.startStamp = startStamp;
      this.endStamp = endStamp;
    }

    public RdsOperatorActivity build(){
      return new RdsOperatorActivity(this);
    }

  }


}


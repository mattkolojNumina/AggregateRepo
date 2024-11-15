package records;


public class ProOperatorLog {

  private String operatorId;
  private String task;
  private String area;
  private java.sql.Timestamp startTime;
  private java.sql.Timestamp loginUploaded;
  private java.sql.Timestamp endTime;
  private java.sql.Timestamp logoffUploaded;


  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }


  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }


  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }


  public java.sql.Timestamp getStartTime() {
    return startTime;
  }

  public void setStartTime(java.sql.Timestamp startTime) {
    this.startTime = startTime;
  }


  public java.sql.Timestamp getLoginUploaded() {
    return loginUploaded;
  }

  public void setLoginUploaded(java.sql.Timestamp loginUploaded) {
    this.loginUploaded = loginUploaded;
  }


  public java.sql.Timestamp getEndTime() {
    return endTime;
  }

  public void setEndTime(java.sql.Timestamp endTime) {
    this.endTime = endTime;
  }


  public java.sql.Timestamp getLogoffUploaded() {
    return logoffUploaded;
  }

  public void setLogoffUploaded(java.sql.Timestamp logoffUploaded) {
    this.logoffUploaded = logoffUploaded;
  }

}

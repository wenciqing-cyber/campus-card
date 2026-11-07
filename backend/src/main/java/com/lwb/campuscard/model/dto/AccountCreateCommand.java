package com.lwb.campuscard.model.dto;

/**
 * 开户请求
 */
public class AccountCreateCommand {

    private String studentNo;
    private String studentName;
    private String dept;
    /** 初始支付密码，不传就用默认的 123456 */
    private String payPwd;

    public String getStudentNo() { return studentNo; }

    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getStudentName() { return studentName; }

    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDept() { return dept; }

    public void setDept(String dept) { this.dept = dept; }

    public String getPayPwd() { return payPwd; }

    public void setPayPwd(String payPwd) { this.payPwd = payPwd; }
}

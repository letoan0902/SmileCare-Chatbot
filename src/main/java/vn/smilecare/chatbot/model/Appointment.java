package vn.smilecare.chatbot.model;


public class Appointment {

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    private final Long id;
    private final String customerName;
    private final String phone;
    private final String doctorName;
    private final String serviceName;
    private String date;
    private String time;
    private Status status;

    public Appointment(
            Long id,
            String customerName,
            String phone,
            String doctorName,
            String serviceName,
            String date,
            String time,
            Status status) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.doctorName = doctorName;
        this.serviceName = serviceName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

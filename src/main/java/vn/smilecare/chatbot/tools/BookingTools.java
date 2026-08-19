package vn.smilecare.chatbot.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import vn.smilecare.chatbot.data.ClinicDataStore;
import vn.smilecare.chatbot.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class BookingTools {

    private final ClinicDataStore clinicDataStore;

    public BookingTools(ClinicDataStore clinicDataStore) {
        this.clinicDataStore = clinicDataStore;
    }

    public record BookingRequest(
            @JsonPropertyDescription("Tên khách hàng")
            String customerName,
            @JsonPropertyDescription("Số điện thoại")
            String phone,
            @JsonPropertyDescription("Tên bác sĩ")
            String doctorName,
            @JsonPropertyDescription("Tên dịch vụ")
            String serviceName,
            @JsonPropertyDescription("Ngày khám định dạng yyyy-MM-dd")
            String date,
            @JsonPropertyDescription("Giờ khám định dạng HH:mm")
            String time) {
    }

    public record BookingResponse(boolean isSuccess, Long appointmentId, String message) {
        public static BookingResponse ok(Long appointmentId, String message) {
            return new BookingResponse(true, appointmentId, message);
        }

        public static BookingResponse error(String message) {
            return new BookingResponse(false, null, message);
        }
    }

    @Tool(description = "Đặt lịch hẹn mới, chỉ gọi khi đã đủ 6 thông tin; nếu isSuccess = false thì đọc message và hỏi lại khách.")
    public BookingResponse bookAppointment(BookingRequest request) {
        if (request == null) {
            return BookingResponse.error("Yêu cầu không hợp lệ.");
        }
        if (request.customerName() == null || request.customerName().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp tên khách hàng.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp số điện thoại.");
        }
        if (request.doctorName() == null || request.doctorName().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp tên bác sĩ.");
        }
        if (request.serviceName() == null || request.serviceName().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp tên dịch vụ.");
        }
        if (request.date() == null || request.date().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp ngày khám.");
        }
        if (request.time() == null || request.time().isBlank()) {
            return BookingResponse.error("Vui lòng cung cấp giờ khám.");
        }

        try {
            LocalDate parsedDate = LocalDate.parse(request.date(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (parsedDate.isBefore(LocalDate.now())) {
                return BookingResponse.error("Ngày khám không được ở trong quá khứ.");
            }
        } catch (DateTimeParseException e) {
            return BookingResponse.error("Định dạng ngày không hợp lệ. Vui lòng nhập theo định dạng yyyy-MM-dd.");
        }

        try {
            LocalTime.parse(request.time(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return BookingResponse.error("Định dạng giờ không hợp lệ. Vui lòng nhập theo định dạng HH:mm.");
        }

        if (clinicDataStore.findDoctorByName(request.doctorName()).isEmpty()) {
            return BookingResponse.error("Bác sĩ không tồn tại trong hệ thống.");
        }

        if (clinicDataStore.findServiceByName(request.serviceName()).isEmpty()) {
            return BookingResponse.error("Dịch vụ không tồn tại trong hệ thống.");
        }

        if (!clinicDataStore.getWorkingHours().contains(request.time())) {
            return BookingResponse.error("Giờ khám không nằm trong khung giờ làm việc của phòng khám.");
        }

        List<Appointment> existingAppointments = clinicDataStore.findConfirmedByDoctorAndDate(request.doctorName(), request.date());
        for (Appointment appt : existingAppointments) {
            if (appt.getTime().equals(request.time())) {
                return BookingResponse.error("Bác sĩ đã có lịch khám vào giờ này, vui lòng chọn giờ khác.");
            }
        }

        Appointment newAppt = new Appointment(null, request.customerName(), request.phone(), request.doctorName(), request.serviceName(), request.date(), request.time(), Appointment.Status.CONFIRMED);
        Appointment savedAppt = clinicDataStore.saveAppointment(newAppt);

        return BookingResponse.ok(savedAppt.getId(), "Đặt lịch thành công cho khách hàng " + request.customerName() + ".");
    }

    @Tool(description = "Đổi ngày giờ của lịch hẹn đã có theo mã lịch hẹn")
    public BookingResponse rescheduleAppointment(Long appointmentId, String newDate, String newTime) {
        if (appointmentId == null) {
            return BookingResponse.error("Thiếu mã lịch hẹn.");
        }
        if (newDate == null || newDate.isBlank()) {
            return BookingResponse.error("Thiếu ngày hẹn mới.");
        }
        if (newTime == null || newTime.isBlank()) {
            return BookingResponse.error("Thiếu giờ hẹn mới.");
        }

        try {
            LocalDate parsedDate = LocalDate.parse(newDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (parsedDate.isBefore(LocalDate.now())) {
                return BookingResponse.error("Ngày khám mới không được ở trong quá khứ.");
            }
        } catch (DateTimeParseException e) {
            return BookingResponse.error("Định dạng ngày mới không hợp lệ (yyyy-MM-dd).");
        }

        try {
            LocalTime.parse(newTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return BookingResponse.error("Định dạng giờ mới không hợp lệ (HH:mm).");
        }

        if (!clinicDataStore.getWorkingHours().contains(newTime)) {
            return BookingResponse.error("Giờ khám mới không nằm trong khung giờ làm việc.");
        }

        Optional<Appointment> optionalAppt = clinicDataStore.findAppointmentById(appointmentId);
        if (optionalAppt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy lịch hẹn với mã này.");
        }

        Appointment appt = optionalAppt.get();
        if (Appointment.Status.CANCELLED.equals(appt.getStatus())) {
            return BookingResponse.error("Lịch hẹn này đã bị huỷ trước đó.");
        }

        List<Appointment> existingAppointments = clinicDataStore.findConfirmedByDoctorAndDate(appt.getDoctorName(), newDate);
        for (Appointment existingAppt : existingAppointments) {
            if (!existingAppt.getId().equals(appointmentId) && existingAppt.getTime().equals(newTime)) {
                return BookingResponse.error("Bác sĩ đã có lịch khám vào giờ mới này, vui lòng chọn giờ khác.");
            }
        }

        appt.setDate(newDate);
        appt.setTime(newTime);
        
        return BookingResponse.ok(appt.getId(), "Đổi lịch thành công sang " + newTime + " ngày " + newDate + ".");
    }

    @Tool(description = "Hủy lịch hẹn theo mã lịch hẹn")
    public BookingResponse cancelAppointment(Long appointmentId) {
        if (appointmentId == null) {
            return BookingResponse.error("Thiếu mã lịch hẹn.");
        }

        Optional<Appointment> optionalAppt = clinicDataStore.findAppointmentById(appointmentId);
        if (optionalAppt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy lịch hẹn với mã này.");
        }

        Appointment appt = optionalAppt.get();
        if (Appointment.Status.CANCELLED.equals(appt.getStatus())) {
            return BookingResponse.error("Lịch hẹn này đã bị huỷ trước đó.");
        }

        appt.setStatus(Appointment.Status.CANCELLED);
        
        return BookingResponse.ok(appt.getId(), "Huỷ lịch hẹn thành công.");
    }
}

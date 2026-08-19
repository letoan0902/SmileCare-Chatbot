package vn.smilecare.chatbot.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import vn.smilecare.chatbot.data.ClinicDataStore;
import vn.smilecare.chatbot.model.Appointment;
import vn.smilecare.chatbot.model.DentalService;
import vn.smilecare.chatbot.model.Doctor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class LookupTools {

    private final ClinicDataStore clinicDataStore;

    public LookupTools(ClinicDataStore clinicDataStore) {
        this.clinicDataStore = clinicDataStore;
    }

    
    public record ToolResult(boolean isSuccess, Object data, String message) {
        public static ToolResult success(Object data, String message) {
            return new ToolResult(true, data, message);
        }

        public static ToolResult error(String message) {
            return new ToolResult(false, null, message);
        }
    }

    
    @Tool(description = "Tìm danh sách bác sĩ của phòng khám theo chuyên khoa (ví dụ: chỉnh nha, implant, nha tổng quát, thẩm mỹ). Truyền chuỗi rỗng nếu khách muốn xem tất cả bác sĩ.")
    public ToolResult findDoctors(
            @JsonPropertyDescription("Chuyên khoa cần tìm (chỉnh nha, implant, nha tổng quát, thẩm mỹ). Truyền chuỗi rỗng nếu khách muốn xem tất cả bác sĩ.")
            String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            List<Doctor> allDoctors = clinicDataStore.findAllDoctors();
            if (allDoctors == null || allDoctors.isEmpty()) {
                return ToolResult.error("Hiện tại phòng khám chưa có danh sách bác sĩ.");
            }
            return ToolResult.success(allDoctors, "Danh sách toàn bộ bác sĩ tại phòng khám SmileCare.");
        }

        String searchSpecialty = specialty.trim();
        List<Doctor> matchedDoctors = clinicDataStore.findDoctorsBySpecialty(searchSpecialty);
        if (matchedDoctors != null && !matchedDoctors.isEmpty()) {
            return ToolResult.success(matchedDoctors, "Tìm thấy " + matchedDoctors.size() + " bác sĩ thuộc chuyên khoa '" + searchSpecialty + "'.");
        }

        List<Doctor> allDoctors = clinicDataStore.findAllDoctors();
        Set<String> existingSpecialties = (allDoctors != null)
                ? allDoctors.stream().map(Doctor::specialty).collect(Collectors.toSet())
                : Collections.emptySet();

        String availableList = existingSpecialties.isEmpty()
                ? "chỉnh nha, implant, nha tổng quát, thẩm mỹ"
                : String.join(", ", existingSpecialties);

        return ToolResult.error("Không tìm thấy bác sĩ nào thuộc chuyên khoa '" + searchSpecialty + "'. Các chuyên khoa hiện có tại phòng khám bao gồm: " + availableList + ".");
    }

    
    @Tool(description = "Tra cứu danh sách dịch vụ nha khoa và bảng giá hiện hành của phòng khám.")
    public ToolResult getDentalServices() {
        List<DentalService> services = clinicDataStore.findAllServices();
        if (services == null || services.isEmpty()) {
            return ToolResult.error("Hiện tại chưa có dữ liệu danh sách dịch vụ nha khoa.");
        }
        return ToolResult.success(services, "Danh sách dịch vụ nha khoa và bảng giá niêm yết tại SmileCare.");
    }

    
    @Tool(description = "Tra khung giờ còn trống của một bác sĩ trong một ngày (date theo định dạng yyyy-MM-dd).")
    public ToolResult getAvailableSlots(
            @JsonPropertyDescription("Tên đầy đủ của bác sĩ cần tra cứu") String doctorName,
            @JsonPropertyDescription("Ngày cần tra cứu theo định dạng yyyy-MM-dd (ví dụ: 2026-08-20)") String date) {

        if (doctorName == null || doctorName.trim().isEmpty()) {
            return ToolResult.error("Vui lòng cung cấp tên bác sĩ để tra cứu khung giờ trống.");
        }

        if (date == null || date.trim().isEmpty()) {
            return ToolResult.error("Vui lòng cung cấp ngày cần tra cứu theo định dạng yyyy-MM-dd.");
        }

        String cleanDate = date.trim();
        if (!cleanDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return ToolResult.error("Định dạng ngày không hợp lệ. Vui lòng nhập ngày theo định dạng yyyy-MM-dd (ví dụ: 2026-08-20).");
        }

        try {
            LocalDate.parse(cleanDate);
        } catch (DateTimeParseException e) {
            return ToolResult.error("Ngày '" + cleanDate + "' không hợp lệ trên lịch. Vui lòng nhập lại ngày theo định dạng yyyy-MM-dd.");
        }

        String searchDoctorName = doctorName.trim();
        Optional<Doctor> doctorOpt = clinicDataStore.findDoctorByName(searchDoctorName);
        if (doctorOpt.isEmpty()) {
            return ToolResult.error("Không tìm thấy bác sĩ '" + searchDoctorName + "' trong hệ thống phòng khám SmileCare.");
        }

        Doctor doctor = doctorOpt.get();
        String targetDoctorName = doctor.name();

        List<String> workingHours = clinicDataStore.getWorkingHours();
        if (workingHours == null || workingHours.isEmpty()) {
            return ToolResult.error("Chưa có thông tin khung giờ làm việc của phòng khám.");
        }

        List<Appointment> confirmedAppointments = clinicDataStore.findConfirmedByDoctorAndDate(targetDoctorName, cleanDate);
        Set<String> bookedTimes = (confirmedAppointments != null)
                ? confirmedAppointments.stream().map(Appointment::getTime).collect(Collectors.toSet())
                : Collections.emptySet();

        List<String> availableSlots = workingHours.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());

        if (availableSlots.isEmpty()) {
            return ToolResult.success(Collections.emptyList(), "Bác sĩ " + targetDoctorName + " không còn khung giờ trống nào trong ngày " + cleanDate + ". Vui lòng chọn ngày khác hoặc bác sĩ khác.");
        }

        return ToolResult.success(availableSlots, "Danh sách khung giờ còn trống của bác sĩ " + targetDoctorName + " ngày " + cleanDate + ": " + String.join(", ", availableSlots));
    }
}


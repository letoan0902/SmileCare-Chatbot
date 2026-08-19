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

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: PHƯƠNG LINH (nhánh feature/lookup-tools)
 * NHIỆM VỤ: Ba @Tool tra cứu - bác sĩ, dịch vụ, lịch trống.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @Service, inject ClinicDataStore qua constructor
 *    (do Văn Vượng làm - cứ khai báo đúng kiểu, Spring tự tiêm).
 *
 * 2. Viết 3 phương thức gắn @Tool (import
 *    org.springframework.ai.tool.annotation.Tool):
 *
 *    Tool 1 - findDoctors(String specialty)
 *    - description gợi ý: "Tìm danh sách bác sĩ của phòng khám theo chuyên
 *      khoa (ví dụ: chỉnh nha, implant, nha tổng quát, thẩm mỹ). Truyền
 *      chuỗi rỗng nếu khách muốn xem tất cả bác sĩ."
 *    - specialty null/rỗng: trả toàn bộ; có giá trị: lọc không phân biệt
 *      hoa thường; không khớp chuyên khoa nào: trả message liệt kê các
 *      chuyên khoa hiện có để mô hình gợi ý lại khách.
 *
 *    Tool 2 - getDentalServices()
 *    - description gợi ý: "Tra cứu danh sách dịch vụ nha khoa và bảng giá
 *      hiện hành của phòng khám."
 *    - Trả danh sách tên dịch vụ kèm giá.
 *
 *    Tool 3 - getAvailableSlots(String doctorName, String date)
 *    - description phải nêu rõ định dạng: "date theo định dạng yyyy-MM-dd".
 *    - Validate phòng thủ: doctorName null/rỗng, date sai định dạng
 *      (regex ^\\d{4}-\\d{2}-\\d{2}$ rồi mới LocalDate.parse trong
 *      try-catch), bác sĩ không tồn tại - mỗi trường hợp trả message lỗi
 *      rõ ràng, KHÔNG ném exception.
 *    - Hợp lệ: lấy khung giờ làm việc từ ClinicDataStore, loại các giờ đã
 *      có lịch hẹn CONFIRMED của bác sĩ đó trong ngày đó, trả danh sách
 *      giờ trống.
 *
 * 3. Kiểu trả về: nên tự khai báo các record kết quả nhỏ ngay trong file
 *    này (ví dụ record ToolResult(boolean isSuccess, Object data,
 *    String message)) hoặc trả chuỗi mô tả - thống nhất nguyên tắc
 *    isSuccess + message như README mục 1.3.
 *
 * NGUYÊN TẮC BẤT DI BẤT DỊCH: không throw trong bất kỳ tool nào - lỗi
 * cũng là kết quả trả về cho mô hình đọc. KHÔNG sửa file của thành viên khác.
 */
@Service
public class LookupTools {

    private final ClinicDataStore clinicDataStore;

    public LookupTools(ClinicDataStore clinicDataStore) {
        this.clinicDataStore = clinicDataStore;
    }

    /**
     * Structure chuẩn trả về cho LLM theo nguyên tắc isSuccess + message.
     */
    public record ToolResult(boolean isSuccess, Object data, String message) {
        public static ToolResult success(Object data, String message) {
            return new ToolResult(true, data, message);
        }

        public static ToolResult error(String message) {
            return new ToolResult(false, null, message);
        }
    }

    /**
     * Tool 1 - findDoctors(String specialty)
     * Tìm danh sách bác sĩ của phòng khám theo chuyên khoa.
     */
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

        // Không khớp chuyên khoa nào: liệt kê các chuyên khoa hiện có để mô hình gợi ý lại khách
        List<Doctor> allDoctors = clinicDataStore.findAllDoctors();
        Set<String> existingSpecialties = (allDoctors != null)
                ? allDoctors.stream().map(Doctor::specialty).collect(Collectors.toSet())
                : Collections.emptySet();

        String availableList = existingSpecialties.isEmpty()
                ? "chỉnh nha, implant, nha tổng quát, thẩm mỹ"
                : String.join(", ", existingSpecialties);

        return ToolResult.error("Không tìm thấy bác sĩ nào thuộc chuyên khoa '" + searchSpecialty + "'. Các chuyên khoa hiện có tại phòng khám bao gồm: " + availableList + ".");
    }

    /**
     * Tool 2 - getDentalServices()
     * Tra cứu danh sách dịch vụ nha khoa và bảng giá hiện hành của phòng khám.
     */
    @Tool(description = "Tra cứu danh sách dịch vụ nha khoa và bảng giá hiện hành của phòng khám.")
    public ToolResult getDentalServices() {
        List<DentalService> services = clinicDataStore.findAllServices();
        if (services == null || services.isEmpty()) {
            return ToolResult.error("Hiện tại chưa có dữ liệu danh sách dịch vụ nha khoa.");
        }
        return ToolResult.success(services, "Danh sách dịch vụ nha khoa và bảng giá niêm yết tại SmileCare.");
    }

    /**
     * Tool 3 - getAvailableSlots(String doctorName, String date)
     * Tra khung giờ còn trống của một bác sĩ trong một ngày (date theo định dạng yyyy-MM-dd).
     */
    @Tool(description = "Tra khung giờ còn trống của một bác sĩ trong một ngày (date theo định dạng yyyy-MM-dd).")
    public ToolResult getAvailableSlots(
            @JsonPropertyDescription("Tên đầy đủ của bác sĩ cần tra cứu") String doctorName,
            @JsonPropertyDescription("Ngày cần tra cứu theo định dạng yyyy-MM-dd (ví dụ: 2026-08-20)") String date) {

        // Validate doctorName
        if (doctorName == null || doctorName.trim().isEmpty()) {
            return ToolResult.error("Vui lòng cung cấp tên bác sĩ để tra cứu khung giờ trống.");
        }

        // Validate date
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

        // Validate doctor existence
        String searchDoctorName = doctorName.trim();
        Optional<Doctor> doctorOpt = clinicDataStore.findDoctorByName(searchDoctorName);
        if (doctorOpt.isEmpty()) {
            return ToolResult.error("Không tìm thấy bác sĩ '" + searchDoctorName + "' trong hệ thống phòng khám SmileCare.");
        }

        Doctor doctor = doctorOpt.get();
        String targetDoctorName = doctor.name();

        // Lấy danh sách giờ làm việc và lịch hẹn đã CONFIRMED của bác sĩ trong ngày
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


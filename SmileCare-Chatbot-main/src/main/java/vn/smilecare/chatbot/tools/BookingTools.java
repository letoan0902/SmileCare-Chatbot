package vn.smilecare.chatbot.tools;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: ĐĂNG VIỆT (nhánh feature/booking-tools)
 * NHIỆM VỤ: Ba @Tool giao dịch - đặt lịch, đổi lịch, hủy lịch.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @Service, inject ClinicDataStore qua constructor.
 *
 * 2. Khai báo 2 record ngay trong file này:
 *
 *    public record BookingRequest(
 *            String customerName, String phone, String doctorName,
 *            String serviceName, String date, String time) {}
 *    - Gắn @JsonPropertyDescription (import từ com.fasterxml.jackson
 *      .annotation) cho từng trường để JSON Schema gửi mô hình nêu rõ
 *      định dạng: date là yyyy-MM-dd, time là HH:mm.
 *
 *    public record BookingResponse(
 *            boolean isSuccess, Long appointmentId, String message) {
 *        // nên thêm static factory ok(...) và error(...)
 *    }
 *
 * 3. Viết 3 phương thức gắn @Tool:
 *
 *    Tool 1 - bookAppointment(BookingRequest request)
 *    - description phải nêu: chỉ gọi khi đã đủ 6 thông tin; nếu
 *      isSuccess = false thì đọc message và hỏi lại khách.
 *    - Validate phòng thủ theo thứ tự: request null; từng trường
 *      null/blank (mỗi trường một message riêng để mô hình biết hỏi gì);
 *      định dạng date (regex + parse try-catch), định dạng time (HH:mm);
 *      ngày không ở quá khứ; bác sĩ tồn tại; dịch vụ tồn tại; giờ nằm
 *      trong khung làm việc; RÀNG BUỘC QUAN TRỌNG NHẤT: bác sĩ chưa có
 *      lịch CONFIRMED trùng ngày giờ đó (chống trùng giờ).
 *    - Hợp lệ: tạo Appointment qua ClinicDataStore, trả ok kèm mã lịch hẹn
 *      và message xác nhận đầy đủ thông tin.
 *
 *    Tool 2 - rescheduleAppointment(Long appointmentId, String newDate,
 *                                   String newTime)
 *    - Validate: id tồn tại, lịch chưa bị hủy, ngày giờ mới hợp lệ và
 *      không trùng lịch khác của cùng bác sĩ; hợp lệ thì cập nhật và trả
 *      ok kèm thông tin mới.
 *
 *    Tool 3 - cancelAppointment(Long appointmentId)
 *    - Validate: id tồn tại, lịch chưa bị hủy trước đó; hợp lệ thì đổi
 *      status sang CANCELLED và trả ok.
 *
 * 4. Import chính: org.springframework.ai.tool.annotation.Tool,
 *    org.springframework.stereotype.Service, java.time.*.
 *
 * NGUYÊN TẮC BẤT DI BẤT DỊCH: không throw trong bất kỳ tool nào - mọi
 * nhánh lỗi trả BookingResponse.error(message) để mô hình đọc và phản
 * hồi khách. KHÔNG sửa file của thành viên khác.
 */
public class BookingTools {

    // TODO (Đăng Việt): xóa dòng ghi chú này và triển khai theo hướng dẫn phía trên
}

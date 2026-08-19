package vn.smilecare.chatbot.tools;

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
public class LookupTools {

    // TODO (Phương Linh): xóa dòng ghi chú này và triển khai theo hướng dẫn phía trên
}

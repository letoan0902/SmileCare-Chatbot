package vn.smilecare.chatbot.model;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: VĂN VƯỢNG (nhánh feature/data-layer)
 * NHIỆM VỤ: Lớp lịch hẹn - đối tượng nghiệp vụ trung tâm.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 * - Đây nên là class thường (không phải record) vì trạng thái thay đổi
 *   được: đổi ngày giờ khi reschedule, đổi status khi hủy.
 * - Trường cần có: Long id; String customerName; String phone;
 *   String doctorName; String serviceName; String date (yyyy-MM-dd);
 *   String time (HH:mm); Status status.
 * - Khai báo enum Status { CONFIRMED, CANCELLED } ngay trong class.
 * - Viết constructor đủ trường, getter cho tất cả, setter cho date,
 *   time, status (chỉ những gì cho phép thay đổi).
 */
public class Appointment {

    // TODO (Văn Vượng): triển khai theo hướng dẫn rồi xóa ghi chú này
}

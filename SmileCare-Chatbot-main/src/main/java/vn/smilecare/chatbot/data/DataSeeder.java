package vn.smilecare.chatbot.data;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: VĂN VƯỢNG (nhánh feature/data-layer)
 * NHIỆM VỤ: Nạp dữ liệu mẫu khi ứng dụng khởi động.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @Component và implement CommandLineRunner
 *    (import org.springframework.boot.CommandLineRunner), inject
 *    ClinicDataStore qua constructor, nạp dữ liệu trong run().
 *
 * 2. Dữ liệu mẫu đã thống nhất:
 *    - 4 bác sĩ: Nguyễn Thị Mai (chỉnh nha), Trần Văn Khoa (implant),
 *      Lê Thị Hồng (nha tổng quát), Phạm Đức Long (thẩm mỹ).
 *    - 6 dịch vụ: Khám tổng quát 200000, Lấy cao răng 300000,
 *      Trám răng 500000, Nhổ răng khôn 1500000, Niềng răng (tư vấn)
 *      500000, Tẩy trắng răng 2500000.
 *    - Nên tạo sẵn 1-2 lịch hẹn mẫu để tool tra lịch trống có dữ liệu
 *      loại trừ ngay từ đầu (ví dụ bác sĩ Nguyễn Thị Mai đã kín giờ
 *      09:00 một ngày gần đây).
 *
 * KHÔNG sửa file của thành viên khác.
 */
public class DataSeeder {

    // TODO (Văn Vượng): triển khai theo hướng dẫn rồi xóa ghi chú này
}

package vn.smilecare.chatbot.model;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: VĂN VƯỢNG (nhánh feature/data-layer)
 * NHIỆM VỤ: Record thông tin bác sĩ.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 * - Chuyển thành record với 3 thành phần: Long id, String name,
 *   String specialty (chuyên khoa: chỉnh nha, implant, nha tổng quát,
 *   thẩm mỹ).
 * - Record là bất biến, không cần thêm gì khác; các tool sẽ trả record
 *   này trực tiếp cho mô hình đọc.
 */
public record Doctor() {

    // TODO (Văn Vượng): thêm 3 thành phần theo hướng dẫn rồi xóa ghi chú này
}

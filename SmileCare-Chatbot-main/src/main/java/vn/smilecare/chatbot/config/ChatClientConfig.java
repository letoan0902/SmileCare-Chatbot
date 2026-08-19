package vn.smilecare.chatbot.config;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: DUY MINH (nhánh feature/chatclient)
 * NHIỆM VỤ: Cấu hình bean ChatClient dùng chung cho toàn ứng dụng.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class này là @Configuration.
 *
 * 2. Viết hằng số SYSTEM_PROMPT (text block) định hình vai trò trợ lý,
 *    cần đủ các phần:
 *    - VAI TRÒ: lễ tân ảo của phòng khám nha khoa SmileCare, thân thiện,
 *      trả lời tiếng Việt, xưng "em" với khách.
 *    - NHIỆM VỤ: tư vấn bác sĩ/dịch vụ, tra lịch trống, đặt/đổi/hủy lịch
 *      bằng cách gọi các tool được cung cấp; không tự bịa thông tin
 *      ngoài kết quả tool.
 *    - QUY TẮC ĐẶT LỊCH: trước khi gọi tool đặt lịch phải đủ 6 thông tin
 *      (tên khách, số điện thoại, bác sĩ, dịch vụ, ngày, giờ); thiếu thông
 *      tin nào thì hỏi khách đúng thông tin đó, không hỏi lại thứ khách
 *      đã nói trong hội thoại.
 *    - NGỮ CẢNH THỜI GIAN: chèn placeholder ngày hiện tại nếu nhóm muốn
 *      hỗ trợ khách nói "ngày mai" (tham khảo cách truyền tham số động).
 *    - RÀNG BUỘC THAM SỐ: ngày truyền vào tool định dạng yyyy-MM-dd,
 *      giờ định dạng HH:mm; tool trả isSuccess = false thì đọc message
 *      và phản hồi khách, không tự chế kết quả.
 *
 * 3. Khai báo bean:
 *
 *    @Bean
 *    public ChatClient chatClient(ChatClient.Builder builder,
 *                                 LookupTools lookupTools,
 *                                 BookingTools bookingTools,
 *                                 ChatMemory chatMemory) { ... }
 *
 *    Bên trong dùng builder với:
 *    - .defaultSystem(SYSTEM_PROMPT)
 *    - .defaultTools(lookupTools, bookingTools)
 *    - .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
 *    - .defaultOptions(ChatOptions.builder().temperature(0.3).build())
 *      (nhiệt độ thấp để tool được gọi ổn định, có thể thử 0.2 - 0.5)
 *
 * 4. Import cần dùng (Spring AI 1.0 GA):
 *    org.springframework.ai.chat.client.ChatClient
 *    org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
 *    org.springframework.ai.chat.memory.ChatMemory
 *    org.springframework.ai.chat.prompt.ChatOptions
 *    org.springframework.context.annotation.Bean / Configuration
 *
 * LƯU Ý PHỐI HỢP: bean ChatMemory do Phương Anh cung cấp, hai class tool
 * do Phương Linh và Đăng Việt cung cấp - chỉ cần khai báo đúng kiểu tham số
 * như trên là Spring tự tiêm, không cần chờ code của bạn khác để bắt đầu.
 * KHÔNG sửa file của thành viên khác.
 */
public class ChatClientConfig {

    // TODO (Duy Minh): xóa dòng ghi chú này và triển khai theo hướng dẫn phía trên
}

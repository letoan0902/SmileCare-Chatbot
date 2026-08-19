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

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.smilecare.chatbot.tools.BookingTools;
import vn.smilecare.chatbot.tools.LookupTools;

@Configuration
public class ChatClientConfig {
    /**
     * System prompt dùng chung cho toàn bộ hội thoại của SmileCare.
     *
     * Mục tiêu của prompt:
     * - Định hình chatbot là lễ tân ảo của phòng khám.
     * - Buộc mô hình ưu tiên sử dụng tool thay vì tự bịa dữ liệu nghiệp vụ.
     * - Hướng dẫn mô hình thu thập đủ 6 thông tin trước khi đặt lịch.
     * - Tận dụng lịch sử hội thoại do ChatMemory cung cấp để không hỏi lại.
     * - Chuẩn hóa định dạng ngày/giờ trước khi truyền vào tool.
     */
    private static final String SYSTEM_PROMPT = """
            Bạn là lễ tân ảo của phòng khám nha khoa SmileCare.

            VAI TRÒ VÀ CÁCH GIAO TIẾP
            - Luôn trả lời bằng tiếng Việt, lịch sự, thân thiện, ngắn gọn và dễ hiểu.
            - Xưng "em" và gọi người dùng là "anh/chị" hoặc "khách hàng" khi phù hợp.
            - Chỉ hỗ trợ các nội dung liên quan đến phòng khám SmileCare và các nghiệp vụ được cung cấp qua tool.
            - Không tự bịa tên bác sĩ, chuyên khoa, dịch vụ, bảng giá, lịch trống, mã lịch hẹn hoặc kết quả đặt lịch.
            - Khi cần dữ liệu nghiệp vụ, phải sử dụng tool phù hợp và dựa vào kết quả tool để trả lời.

            CÁC NHIỆM VỤ ĐƯỢC HỖ TRỢ
            1. Tư vấn và tra cứu bác sĩ theo chuyên khoa.
            2. Tra cứu dịch vụ nha khoa và bảng giá.
            3. Tra cứu khung giờ còn trống của bác sĩ theo ngày.
            4. Đặt lịch hẹn mới.
            5. Đổi ngày hoặc giờ của lịch hẹn đã đặt.
            6. Hủy lịch hẹn đã đặt.

            QUY TẮC SỬ DỤNG TOOL
            - Khi khách hỏi về bác sĩ, sử dụng tool findDoctors.
            - Khi khách hỏi về dịch vụ hoặc giá, sử dụng tool getDentalServices.
            - Khi khách hỏi lịch trống, sử dụng tool getAvailableSlots.
            - Khi khách muốn đặt lịch, chỉ gọi tool bookAppointment khi đã có đủ 6 thông tin bắt buộc:
              (1) tên khách hàng,
              (2) số điện thoại,
              (3) tên bác sĩ,
              (4) tên dịch vụ,
              (5) ngày khám,
              (6) giờ khám.
            - Nếu chưa đủ 6 thông tin đặt lịch, chỉ hỏi đúng thông tin còn thiếu.
            - Không hỏi lại thông tin khách đã cung cấp ở các lượt trước; phải tận dụng lịch sử hội thoại trong bộ nhớ.
            - Khi khách muốn đổi lịch, cần có mã lịch hẹn, ngày mới và giờ mới trước khi gọi rescheduleAppointment.
            - Khi khách muốn hủy lịch, cần có mã lịch hẹn trước khi gọi cancelAppointment.
            - Không tự mô phỏng kết quả của tool và không khẳng định thao tác đã thành công nếu chưa có kết quả thành công từ tool.

            QUY TẮC VỀ NGÀY VÀ GIỜ
            - Ngày truyền vào tool phải theo đúng định dạng yyyy-MM-dd.
            - Giờ truyền vào tool phải theo đúng định dạng HH:mm.
            - Nếu khách nói ngày hoặc giờ chưa đủ rõ để chuyển chính xác sang các định dạng trên, hãy hỏi lại để xác nhận.
            - Không tự suy đoán một ngày cụ thể khi thông tin thời gian của khách còn mơ hồ.

            XỬ LÝ KẾT QUẢ TOOL
            - Luôn đọc kỹ dữ liệu và trường message mà tool trả về.
            - Nếu isSuccess = true, phản hồi dựa đúng trên kết quả tool.
            - Nếu isSuccess = false, giải thích ngắn gọn lỗi theo trường message và hỏi hoặc đề xuất thông tin cần thiết để khách tiếp tục.
            - Không che giấu lỗi nghiệp vụ và không tự tạo kết quả thay thế cho tool.

            NGUYÊN TẮC HỘI THOẠI NHIỀU LƯỢT
            - Ghi nhận và sử dụng lại tên khách, số điện thoại, bác sĩ, dịch vụ, ngày, giờ và mã lịch hẹn đã xuất hiện trong lịch sử hội thoại.
            - Nếu khách thay đổi một thông tin đã nói trước đó, ưu tiên thông tin mới nhất.
            - Mỗi lần chỉ hỏi phần thông tin thực sự còn thiếu để cuộc hội thoại tự nhiên và không lặp lại.
            """;

    /**
     * Khởi tạo một ChatClient dùng chung cho toàn ứng dụng.
     *
     * Spring sẽ tự inject:
     * - ChatClient.Builder: do Spring AI auto-configuration cung cấp.
     * - LookupTools: nhóm tool tra cứu.
     * - BookingTools: nhóm tool đặt/đổi/hủy lịch.
     * - ChatMemory: bean bộ nhớ hội thoại do ChatMemoryConfig cung cấp.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 LookupTools lookupTools,
                                 BookingTools bookingTools,
                                 ChatMemory chatMemory) {

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.3)
                .build();

        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(lookupTools, bookingTools)
                .defaultAdvisors(memoryAdvisor)
                .defaultOptions(chatOptions)
                .build();
    }
}

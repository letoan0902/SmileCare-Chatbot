package vn.smilecare.chatbot.controller;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: PHƯƠNG ANH (nhánh feature/chatmemory-controller)
 * NHIỆM VỤ: REST endpoint chat nhiều lượt trên cùng một conversationId.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @RestController và @RequestMapping("/api").
 *
 * 2. Khai báo record kết quả ngay trong class:
 *    public record ChatResult(String conversationId, String answer) {}
 *
 * 3. Inject ChatClient qua constructor (bean do Duy Minh cấu hình).
 *
 * 4. Viết endpoint:
 *
 *    @GetMapping("/chat")
 *    public ChatResult chat(@RequestParam String message,
 *                           @RequestParam(required = false) String conversationId)
 *
 *    Logic bên trong:
 *    - Nếu conversationId null hoặc blank: sinh UUID.randomUUID().toString()
 *      làm mã phiên mới (lượt chat đầu tiên của khách).
 *    - Gọi model:
 *      chatClient.prompt()
 *              .user(message)
 *              .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
 *              .call()
 *              .content()
 *    - Trả về ChatResult(sessionId, answer) - client phải nhận lại
 *      conversationId để gửi kèm trong các lượt sau.
 *
 * 5. Import cần dùng:
 *    org.springframework.ai.chat.client.ChatClient
 *    org.springframework.ai.chat.memory.ChatMemory  (hằng CONVERSATION_ID)
 *    org.springframework.web.bind.annotation.*
 *    java.util.UUID
 *
 * 6. Nên thêm một endpoint phụ giúp nhóm test:
 *    @GetMapping("/chat/new") trả về UUID mới - client gọi khi muốn
 *    bắt đầu phiên hoàn toàn mới.
 *
 * KHÔNG sửa file của thành viên khác.
 */
public class ChatController {

    // TODO (Phương Anh): xóa dòng ghi chú này và triển khai theo hướng dẫn phía trên
}

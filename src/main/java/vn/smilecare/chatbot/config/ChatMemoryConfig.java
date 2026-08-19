package vn.smilecare.chatbot.config;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: PHƯƠNG ANH (nhánh feature/chatmemory-controller)
 * NHIỆM VỤ: Cấu hình bean ChatMemory - bộ nhớ hội thoại theo phiên.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @Configuration.
 *
 * 2. Khai báo bean ChatMemory dùng chiến lược cửa sổ trượt
 *    (giữ N tin nhắn gần nhất cho mỗi hội thoại):
 *
 *    @Bean
 *    public ChatMemory chatMemory() {
 *        return MessageWindowChatMemory.builder()
 *                .maxMessages(20)
 *                .build();
 *    }
 *
 *    Không truyền repository thì Spring AI tự dùng kho trong RAM -
 *    đủ cho bài này. Nhóm muốn nâng cấp lưu database thì đổi sang
 *    JdbcChatMemoryRepository sau, chữ ký bean giữ nguyên.
 *
 * 3. Import cần dùng:
 *    org.springframework.ai.chat.memory.ChatMemory
 *    org.springframework.ai.chat.memory.MessageWindowChatMemory
 *    org.springframework.context.annotation.Bean / Configuration
 *
 * 4. Giải thích ngắn để nắm bản chất: advisor bộ nhớ (do Duy Minh gắn
 *    vào ChatClient) sẽ tự đọc lịch sử của conversationId trước mỗi lần
 *    gọi model và tự ghi thêm tin nhắn mới sau khi có phản hồi. Bean này
 *    chỉ quyết định lưu ở đâu và giữ bao nhiêu tin.
 *
 * KHÔNG sửa file của thành viên khác.
 */

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory(ChatMemory chatMemory){
        return MessageWindowChatMemory.builder().maxMessages(20).chatMemoryRepository(new InMemoryChatMemoryRepository()).build();
    }



}

package vn.smilecare.chatbot.controller;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.smilecare.chatbot.model.ChatResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public ChatResult chat(@RequestParam String message, @RequestParam(required = false) String conversationId){
        String sessionId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
        String answer = chatClient.prompt().user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId)).call().content();
        return new ChatResult(sessionId, answer);
    }
    @GetMapping("/chat/new")
    public ChatResult createNewChat(){
        String newConversationId = UUID.randomUUID().toString();
        return new ChatResult(newConversationId, null);
    }
}

# SmileCare Chatbot - Trợ lý tư vấn và đặt lịch khám nha khoa

Dự án nhóm xây dựng chatbot cho phòng khám nha khoa SmileCare bằng Spring Boot kết hợp Spring AI, sử dụng ba trụ cột kỹ thuật: ChatClient (giao tiếp LLM), ChatMemory (ghi nhớ ngữ cảnh theo phiên) và @Tool (function calling để LLM tự gọi nghiệp vụ thật). Mô hình ngôn ngữ nhóm sử dụng là Gemini thông qua Google AI Studio (endpoint tương thích chuẩn OpenAI).

Khung dự án đã được dựng sẵn. Mỗi file mã nguồn có phần comment ghi rõ người phụ trách và hướng dẫn từng bước; thành viên chỉ code trong đúng các file thuộc phần việc của mình để không xung đột khi merge.

## 1. Kết quả thống nhất của Giai đoạn 1

### 1.1. Sáu nghiệp vụ chatbot phải hỗ trợ

1. Tư vấn thông tin bác sĩ theo chuyên khoa (chỉnh nha, implant, nha tổng quát, thẩm mỹ).
2. Tra cứu danh sách dịch vụ và bảng giá.
3. Tra cứu khung giờ còn trống của một bác sĩ theo ngày.
4. Đặt lịch hẹn mới.
5. Đổi lịch hẹn đã đặt sang ngày giờ khác.
6. Hủy lịch hẹn đã đặt.

Xuyên suốt mọi nghiệp vụ, chatbot phải nhớ ngữ cảnh hội thoại (tên khách, bác sĩ, khung giờ đã nhắc trước đó) và chủ động hỏi lại khi thiếu thông tin.

### 1.2. Luồng xử lý đặt lịch đã chốt

Điều kiện đủ để đặt lịch gồm 6 thông tin: tên khách hàng, số điện thoại, bác sĩ, dịch vụ, ngày khám, giờ khám.

- Lượt chat đầy đủ thông tin: khách cung cấp đủ 6 thông tin trong một hoặc nhiều lượt, mô hình gom đủ dữ kiện từ lịch sử hội thoại rồi gọi tool `bookAppointment` một lần duy nhất, đọc kết quả trả về và xác nhận với khách kèm mã lịch hẹn.
- Lượt chat thiếu thông tin: mô hình đối chiếu 6 điều kiện với những gì đã biết trong bộ nhớ hội thoại, chỉ hỏi đúng phần còn thiếu (không hỏi lại thứ khách đã nói). Việc hỏi lại do system prompt cùng cơ chế function calling điều khiển, tuyệt đối không lập trình luồng hỏi cứng bằng if-else. Nếu tool trả về lỗi nghiệp vụ (trùng giờ, bác sĩ không tồn tại), mô hình đọc trường `message` trong kết quả và đề xuất phương án thay thế cho khách.

### 1.3. Danh sách 6 @Tool đã chốt

| Tool | Mô tả cho LLM | Tham số | Trả về |
|---|---|---|---|
| findDoctors | Tìm bác sĩ theo chuyên khoa, bỏ trống thì trả toàn bộ | specialty (String, tùy chọn) | Danh sách bác sĩ kèm chuyên khoa |
| getDentalServices | Tra cứu danh sách dịch vụ và bảng giá của phòng khám | không có | Danh sách dịch vụ kèm giá |
| getAvailableSlots | Tra khung giờ còn trống của một bác sĩ trong một ngày | doctorName (String), date (String, yyyy-MM-dd) | Danh sách giờ trống |
| bookAppointment | Đặt lịch hẹn mới, chỉ gọi khi đã đủ 6 thông tin | BookingRequest (record 6 trường) | BookingResponse có isSuccess, appointmentId, message |
| rescheduleAppointment | Đổi ngày giờ của lịch hẹn đã có theo mã lịch hẹn | appointmentId, newDate, newTime | BookingResponse |
| cancelAppointment | Hủy lịch hẹn theo mã lịch hẹn | appointmentId | BookingResponse |

Quy ước chung cho mọi tool: không bao giờ ném exception; mọi nhánh lỗi (thiếu tham số, sai định dạng ngày, trùng giờ, không tìm thấy) trả về đối tượng response có `isSuccess = false` và `message` mô tả rõ để mô hình đọc và phản hồi khách. Mô tả tool phải nêu định dạng tham số (ngày yyyy-MM-dd, giờ HH:mm) và điều kiện gọi.

### 1.4. Mô hình dữ liệu dùng chung

Lưu in-memory bằng một lớp `ClinicDataStore` duy nhất (không dùng database để cả nhóm tập trung vào Spring AI):

- `Doctor(id, name, specialty)`
- `DentalService(id, name, price)`
- `Appointment(id, customerName, phone, doctorName, serviceName, date, time, status)` với status là CONFIRMED hoặc CANCELLED.

Dữ liệu mẫu: 4 bác sĩ (đủ 4 chuyên khoa), 6 dịch vụ, giờ làm việc 09:00 đến 17:00, mỗi khung 1 tiếng.

### 1.5. Kiến trúc kỹ thuật đã chốt

- Một bean `ChatClient` duy nhất dùng chung toàn ứng dụng, cấu hình sẵn `defaultSystem` (system prompt định hình vai trò lễ tân ảo SmileCare), `defaultTools` (cả hai nhóm tool) và `defaultAdvisors` (advisor bộ nhớ hội thoại).
- Bộ nhớ hội thoại: `MessageWindowChatMemory` giữ 20 tin nhắn gần nhất cho mỗi phiên, gắn vào ChatClient qua `MessageChatMemoryAdvisor`. Mỗi khách một `conversationId`: client gửi kèm mỗi request; nếu thiếu (lượt đầu) server tự sinh UUID và trả lại trong response để client dùng cho các lượt sau.
- Model: Gemini 2.5 Flash gọi qua endpoint tương thích OpenAI của Google AI Studio. API key đọc từ biến môi trường `GEMINI_API_KEY`, không ghi cứng vào mã nguồn.
- Quy ước package: `vn.smilecare.chatbot` với các package con `config`, `controller`, `model`, `data`, `tools`.
- Quy ước đặt tên: tên tool bằng tiếng Anh dạng camelCase đúng như bảng trên; message trả về khách bằng tiếng Việt.

## 2. Phân công thành viên

| Thành viên | Vai trò | File phụ trách | Nhánh Git |
|---|---|---|---|
| Duy Minh | Cấu hình ChatClient: system prompt, ChatOptions, bean dùng chung | `config/ChatClientConfig.java` | feature/chatclient |
| Phương Anh | ChatMemory và REST endpoint quản lý phiên chat | `config/ChatMemoryConfig.java`, `controller/ChatController.java` | feature/chatmemory-controller |
| Phương Linh | Nhóm tool tra cứu: bác sĩ, dịch vụ, lịch trống | `tools/LookupTools.java` | feature/lookup-tools |
| Đăng Việt | Nhóm tool giao dịch: đặt, đổi, hủy lịch kèm ràng buộc nghiệp vụ | `tools/BookingTools.java` | feature/booking-tools |
| Văn Vượng | Tầng dữ liệu: model, kho dữ liệu in-memory, seed dữ liệu mẫu | `model/Doctor.java`, `model/DentalService.java`, `model/Appointment.java`, `data/ClinicDataStore.java`, `data/DataSeeder.java` | feature/data-layer |
| Tiến Đức | Kịch bản test hội thoại multi-turn, minh chứng demo, giao diện thử nghiệm | `docs/demo-conversations.md`, `requests.http`, `static/index.html` | feature/demo-test |

Nguyên tắc chống xung đột: mỗi người chỉ sửa file trong cột "File phụ trách" của mình. Các file dùng chung (build.gradle, application.yml, class khởi động) đã được dựng sẵn trong khung, không ai tự ý sửa; cần thay đổi thì nêu trong nhóm để thống nhất.

Phụ thuộc giữa các phần và thứ tự merge khuyến nghị: Văn Vượng (data) merge trước tiên vì hai nhóm tool đều gọi vào `ClinicDataStore`; tiếp theo Phương Linh và Đăng Việt (tools); rồi Duy Minh (ChatClient cần tools để khai báo defaultTools); rồi Phương Anh (controller cần ChatClient); cuối cùng Tiến Đức chạy kịch bản test trên bản tích hợp. Trong thời gian chờ merge, các phần đều code được song song vì Spring tiêm phụ thuộc theo kiểu (type) chứ không tham chiếu file của nhau.

## 3. Cấu trúc dự án

```
SmileCare Chatbot/
|-- README.md
|-- build.gradle
|-- settings.gradle
|-- .gitignore
|-- requests.http                          (Tien Duc)
|-- docs/
|   `-- demo-conversations.md              (Tien Duc)
`-- src/main/
    |-- java/vn/smilecare/chatbot/
    |   |-- SmileCareChatbotApplication.java
    |   |-- config/
    |   |   |-- ChatClientConfig.java      (Duy Minh)
    |   |   `-- ChatMemoryConfig.java      (Phuong Anh)
    |   |-- controller/
    |   |   `-- ChatController.java        (Phuong Anh)
    |   |-- model/
    |   |   |-- Doctor.java                (Van Vuong)
    |   |   |-- DentalService.java         (Van Vuong)
    |   |   `-- Appointment.java           (Van Vuong)
    |   |-- data/
    |   |   |-- ClinicDataStore.java       (Van Vuong)
    |   |   `-- DataSeeder.java            (Van Vuong)
    |   `-- tools/
    |       |-- LookupTools.java           (Phuong Linh)
    |       `-- BookingTools.java          (Dang Viet)
    `-- resources/
        |-- application.yml
        `-- static/
            `-- index.html                 (Tien Duc)
```

## 4. Hướng dẫn cài đặt và chạy

1. Yêu cầu: JDK 17 trở lên, Gradle (hoặc dùng wrapper nếu nhóm bổ sung), API key Gemini lấy từ Google AI Studio.
2. Đặt biến môi trường trước khi chạy:

Windows PowerShell:

```powershell
$env:GEMINI_API_KEY = "dien-api-key-cua-nhom"
```

Linux/macOS:

```bash
export GEMINI_API_KEY=dien-api-key-cua-nhom
```

3. Chạy ứng dụng:

```bash
gradle bootRun
```

4. Thử hội thoại nhiều lượt trên cùng một phiên (lượt đầu không truyền conversationId, các lượt sau truyền mã nhận được từ response):

```bash
curl "http://localhost:8080/api/chat?message=Chao%20phong%20kham"
```

## 5. Quy trình làm việc nhóm

1. Mỗi thành viên tạo nhánh riêng từ main theo đúng tên nhánh trong bảng phân công, commit thường xuyên trên nhánh của mình bằng tài khoản Git cá nhân để lịch sử thể hiện rõ đóng góp từng người.
2. Hoàn thành phần việc thì tạo Pull Request; ít nhất một thành viên khác review trước khi merge; merge theo thứ tự khuyến nghị ở mục 2.
3. Sau khi tích hợp đủ, cả nhóm chạy ba kịch bản hội thoại trong `docs/demo-conversations.md`, dán kết quả thật vào file đó làm minh chứng; mỗi thành viên chuẩn bị demo đúng phần mình phụ trách.
4. Thành viên xong sớm hỗ trợ review và tích hợp, không code hộ phần của người khác trên nhánh của họ.

# Kịch bản test hội thoại multi-turn - NGƯỜI PHỤ TRÁCH: TIẾN ĐỨC (nhánh feature/demo-test)

Nhiệm vụ: sau khi cả nhóm merge đủ các phần, chạy ba kịch bản dưới đây trên ứng dụng thật, dán nguyên văn từng lượt hỏi đáp (kèm conversationId) vào từng mục để làm minh chứng demo. Mỗi kịch bản chạy trên MỘT conversationId duy nhất từ đầu đến cuối.

## Kịch bản 1: Nhớ ngữ cảnh xuyên nhiều lượt

Mục tiêu chứng minh: chatbot không hỏi lại thông tin khách đã cung cấp.

- Lượt 1: khách chào và giới thiệu tên kèm số điện thoại.
- Lượt 2: khách hỏi phòng khám có bác sĩ chỉnh nha nào (kỳ vọng: gọi tool findDoctors, trả đúng bác sĩ chuyên khoa chỉnh nha).
- Lượt 3: khách hỏi giá dịch vụ niềng răng (kỳ vọng: gọi tool getDentalServices).
- Lượt 4: khách nói "đặt cho tôi lịch với bác sĩ đó, 10 giờ sáng thứ Hai tuần sau, dịch vụ tư vấn niềng răng" (kỳ vọng: chatbot tự dùng tên và số điện thoại từ lượt 1, bác sĩ từ lượt 2, KHÔNG hỏi lại các thông tin này; gọi bookAppointment và xác nhận kèm mã lịch hẹn).

Kết quả thực tế (dán vào đây):

```
(chưa chạy)
```

## Kịch bản 2: Thiếu thông tin - chatbot chủ động hỏi bổ sung

Mục tiêu chứng minh: cơ chế hỏi lại đến từ function calling kết hợp bộ nhớ, không phải if-else cứng.

- Lượt 1: khách mở đầu bằng câu thiếu gần hết thông tin: "Tôi muốn đặt lịch khám" (kỳ vọng: chatbot hỏi các thông tin còn thiếu, không gọi tool đặt lịch).
- Lượt 2-4: khách nhỏ giọt từng thông tin (tên + số điện thoại, rồi chọn bác sĩ, rồi ngày giờ và dịch vụ); mỗi lượt chatbot chỉ hỏi phần còn thiếu.
- Lượt cuối: đủ thông tin, chatbot gọi bookAppointment và xác nhận.

Kết quả thực tế (dán vào đây):

```
(chưa chạy)
```

## Kịch bản 3: Ràng buộc nghiệp vụ và đổi/hủy lịch

Mục tiêu chứng minh: tool phòng thủ trả lỗi nghiệp vụ và mô hình xử lý mượt.

- Lượt 1: khách cố đặt đúng khung giờ đã kín của bác sĩ (dữ liệu seed sẵn) - kỳ vọng: bookAppointment trả isSuccess=false vì trùng giờ, chatbot đề xuất giờ trống khác (có thể tự gọi getAvailableSlots).
- Lượt 2: khách đồng ý giờ khác - kỳ vọng: đặt thành công.
- Lượt 3: khách đổi lịch vừa đặt sang ngày khác (kỳ vọng: gọi rescheduleAppointment với mã lịch hẹn từ lượt trước, không cần khách nhắc lại mã).
- Lượt 4: khách hủy lịch (kỳ vọng: gọi cancelAppointment, xác nhận đã hủy).

Kết quả thực tế (dán vào đây):

```
(chưa chạy)
```

## Ghi chú cách chạy

Dùng file requests.http (mở bằng IntelliJ/VS Code REST Client) hoặc trang static/index.html sau khi ứng dụng khởi động. Nhớ lấy conversationId từ response lượt đầu và truyền vào mọi lượt sau của cùng kịch bản.

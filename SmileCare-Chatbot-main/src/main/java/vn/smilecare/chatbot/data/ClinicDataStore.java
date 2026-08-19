package vn.smilecare.chatbot.data;

/*
 * ============================================================
 * NGƯỜI PHỤ TRÁCH: VĂN VƯỢNG (nhánh feature/data-layer)
 * NHIỆM VỤ: Kho dữ liệu in-memory dùng chung cho toàn bộ tool.
 * ============================================================
 *
 * HƯỚNG DẪN LÀM:
 *
 * 1. Đánh dấu class là @Component (import org.springframework.stereotype
 *    .Component) để hai class tool inject được.
 *
 * 2. Trường dữ liệu (dùng cấu trúc an toàn luồng vì nhiều request đồng
 *    thời):
 *    - List<Doctor> doctors = new CopyOnWriteArrayList<>();
 *    - List<DentalService> services = new CopyOnWriteArrayList<>();
 *    - Map<Long, Appointment> appointments = new ConcurrentHashMap<>();
 *    - AtomicLong appointmentIdSeq = new AtomicLong(1);
 *    - Hằng WORKING_HOURS: danh sách giờ làm việc cố định
 *      ["09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"].
 *
 * 3. Phương thức cần cung cấp (đây là hợp đồng với Phương Linh và
 *    Đăng Việt - đặt đúng tên để code hai bạn ấy gọi được):
 *    - List<Doctor> findAllDoctors()
 *    - List<Doctor> findDoctorsBySpecialty(String specialty)  (không phân biệt hoa thường, contains)
 *    - Optional<Doctor> findDoctorByName(String name)
 *    - List<DentalService> findAllServices()
 *    - Optional<DentalService> findServiceByName(String name)
 *    - List<String> getWorkingHours()
 *    - List<Appointment> findConfirmedByDoctorAndDate(String doctorName, String date)
 *    - Appointment saveAppointment(...)  (tự sinh id từ appointmentIdSeq)
 *    - Optional<Appointment> findAppointmentById(Long id)
 *    - seed dữ liệu: các phương thức addDoctor/addService cho DataSeeder gọi
 *
 * KHÔNG sửa file của thành viên khác.
 */
public class ClinicDataStore {

    // TODO (Văn Vượng): triển khai theo hướng dẫn rồi xóa ghi chú này
}

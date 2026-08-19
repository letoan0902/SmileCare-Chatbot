package vn.smilecare.chatbot.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.smilecare.chatbot.model.DentalService;
import vn.smilecare.chatbot.model.Doctor;

/*
 * Người phụ trách: Văn Vượng
 * Nạp dữ liệu mẫu khi ứng dụng khởi động.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ClinicDataStore clinicDataStore;

    public DataSeeder(ClinicDataStore clinicDataStore) {
        this.clinicDataStore = clinicDataStore;
    }

    @Override
    public void run(String... args) {
        if (!clinicDataStore.findAllDoctors().isEmpty() || !clinicDataStore.findAllServices().isEmpty()) {
            return;
        }

        clinicDataStore.addDoctor(new Doctor(1L, "Nguyễn Thị Mai", "chỉnh nha"));
        clinicDataStore.addDoctor(new Doctor(2L, "Trần Văn Khoa", "implant"));
        clinicDataStore.addDoctor(new Doctor(3L, "Lê Thị Hồng", "nha tổng quát"));
        clinicDataStore.addDoctor(new Doctor(4L, "Phạm Đức Long", "thẩm mỹ"));

        clinicDataStore.addService(new DentalService(1L, "Khám tổng quát", 200_000L));
        clinicDataStore.addService(new DentalService(2L, "Lấy cao răng", 300_000L));
        clinicDataStore.addService(new DentalService(3L, "Trám răng", 500_000L));
        clinicDataStore.addService(new DentalService(4L, "Nhổ răng khôn", 1_500_000L));
        clinicDataStore.addService(new DentalService(5L, "Niềng răng (tư vấn)", 500_000L));
        clinicDataStore.addService(new DentalService(6L, "Tẩy trắng răng", 2_500_000L));

        clinicDataStore.saveAppointment(
                "Khách mẫu A",
                "0900000001",
                "Nguyễn Thị Mai",
                "Niềng răng (tư vấn)",
                "2026-08-20",
                "09:00"
        );
        clinicDataStore.saveAppointment(
                "Khách mẫu B",
                "0900000002",
                "Trần Văn Khoa",
                "Nhổ răng khôn",
                "2026-08-20",
                "14:00"
        );
    }
}

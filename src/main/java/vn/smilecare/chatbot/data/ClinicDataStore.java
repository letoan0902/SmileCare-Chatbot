package vn.smilecare.chatbot.data;

import org.springframework.stereotype.Component;
import vn.smilecare.chatbot.model.Appointment;
import vn.smilecare.chatbot.model.DentalService;
import vn.smilecare.chatbot.model.Doctor;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class ClinicDataStore {

    private static final List<String> WORKING_HOURS = List.of(
            "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"
    );

    private final List<Doctor> doctors = new CopyOnWriteArrayList<>();
    private final List<DentalService> services = new CopyOnWriteArrayList<>();
    private final Map<Long, Appointment> appointments = new ConcurrentHashMap<>();
    private final AtomicLong appointmentIdSeq = new AtomicLong(1);

    public List<Doctor> findAllDoctors() {
        return List.copyOf(doctors);
    }

    public List<Doctor> findDoctorsBySpecialty(String specialty) {
        if (isBlank(specialty)) {
            return findAllDoctors();
        }

        String normalizedSpecialty = normalize(specialty);
        return doctors.stream()
                .filter(doctor -> normalize(doctor.specialty()).contains(normalizedSpecialty))
                .toList();
    }

    public Optional<Doctor> findDoctorByName(String name) {
        if (isBlank(name)) {
            return Optional.empty();
        }

        String normalizedName = normalize(name);
        Optional<Doctor> exactMatch = doctors.stream()
                .filter(doctor -> normalize(doctor.name()).equals(normalizedName))
                .findFirst();

        return exactMatch.or(() -> doctors.stream()
                .filter(doctor -> normalize(doctor.name()).contains(normalizedName))
                .findFirst());
    }

    public List<DentalService> findAllServices() {
        return List.copyOf(services);
    }

    public Optional<DentalService> findServiceByName(String name) {
        if (isBlank(name)) {
            return Optional.empty();
        }

        String normalizedName = normalize(name);
        Optional<DentalService> exactMatch = services.stream()
                .filter(service -> normalize(service.name()).equals(normalizedName))
                .findFirst();

        return exactMatch.or(() -> services.stream()
                .filter(service -> normalize(service.name()).contains(normalizedName))
                .findFirst());
    }

    public List<String> getWorkingHours() {
        return WORKING_HOURS;
    }

    public boolean isWorkingHour(String time) {
        return WORKING_HOURS.contains(time);
    }

    public List<Appointment> findConfirmedByDoctorAndDate(String doctorName, String date) {
        if (isBlank(doctorName) || isBlank(date)) {
            return List.of();
        }

        String normalizedDoctorName = normalize(doctorName);
        return appointments.values().stream()
                .filter(appointment -> appointment.getStatus() == Appointment.Status.CONFIRMED)
                .filter(appointment -> normalize(appointment.getDoctorName()).equals(normalizedDoctorName))
                .filter(appointment -> appointment.getDate().equals(date))
                .sorted(Comparator.comparing(Appointment::getTime))
                .toList();
    }

    public List<String> findAvailableSlots(String doctorName, String date) {
        List<String> bookedSlots = findConfirmedByDoctorAndDate(doctorName, date).stream()
                .map(Appointment::getTime)
                .toList();

        return WORKING_HOURS.stream()
                .filter(time -> !bookedSlots.contains(time))
                .toList();
    }

    public boolean hasConfirmedAppointment(String doctorName, String date, String time) {
        return hasConfirmedAppointment(doctorName, date, time, null);
    }

    public boolean hasConfirmedAppointment(String doctorName, String date, String time, Long ignoredAppointmentId) {
        if (isBlank(doctorName) || isBlank(date) || isBlank(time)) {
            return false;
        }

        String normalizedDoctorName = normalize(doctorName);
        return appointments.values().stream()
                .filter(appointment -> ignoredAppointmentId == null || !appointment.getId().equals(ignoredAppointmentId))
                .filter(appointment -> appointment.getStatus() == Appointment.Status.CONFIRMED)
                .filter(appointment -> normalize(appointment.getDoctorName()).equals(normalizedDoctorName))
                .filter(appointment -> appointment.getDate().equals(date))
                .anyMatch(appointment -> appointment.getTime().equals(time));
    }

    public Appointment saveAppointment(
            String customerName,
            String phone,
            String doctorName,
            String serviceName,
            String date,
            String time) {
        Long id = appointmentIdSeq.getAndIncrement();
        Appointment appointment = new Appointment(
                id,
                customerName,
                phone,
                doctorName,
                serviceName,
                date,
                time,
                Appointment.Status.CONFIRMED
        );
        appointments.put(id, appointment);
        return appointment;
    }

    public Optional<Appointment> findAppointmentById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(appointments.get(id));
    }

    public List<Appointment> findAllAppointments() {
        return appointments.values().stream()
                .sorted(Comparator.comparing(Appointment::getId))
                .toList();
    }

    public Optional<Appointment> rescheduleAppointment(Long id, String newDate, String newTime) {
        Optional<Appointment> appointmentOptional = findAppointmentById(id);
        appointmentOptional.ifPresent(appointment -> {
            appointment.setDate(newDate);
            appointment.setTime(newTime);
        });
        return appointmentOptional;
    }

    public Optional<Appointment> cancelAppointment(Long id) {
        Optional<Appointment> appointmentOptional = findAppointmentById(id);
        appointmentOptional.ifPresent(appointment -> appointment.setStatus(Appointment.Status.CANCELLED));
        return appointmentOptional;
    }

    public void addDoctor(Doctor doctor) {
        if (doctor != null) {
            doctors.add(doctor);
        }
    }

    public void addService(DentalService service) {
        if (service != null) {
            services.add(service);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}

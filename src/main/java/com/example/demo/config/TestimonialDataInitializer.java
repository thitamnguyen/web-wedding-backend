package com.example.demo.config;

import com.example.demo.model.CustomerTestimonial;
import com.example.demo.repository.CustomerTestimonialRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestimonialDataInitializer implements CommandLineRunner {

    private final CustomerTestimonialRepository repository;

    public TestimonialDataInitializer(CustomerTestimonialRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(
                create("Khánh Linh", 5, "Pre Wedding", "Bộ ảnh lên concept rất khác biệt, nhìn vào là thấy sang và có cảm xúc ngay."),
                create("Minh Quân", 5, "Booking & Setup", "Ekip tư vấn rất kỹ, từ váy cưới đến makeup đều khớp với style mình muốn."),
                create("Thảo Vy", 5, "Bridal Makeup", "Makeup nhẹ nhưng lên hình cực đẹp, cả ngày cưới vẫn rất tươi."),
                create("Thanh Trúc", 4, "Concept Studio", "Mình thích cách studio biến câu chuyện riêng thành concept riêng biệt.")
        ));
    }

    private CustomerTestimonial create(String name, int rating, String serviceType, String feedback) {
        CustomerTestimonial testimonial = new CustomerTestimonial();
        testimonial.setCustomerName(name);
        testimonial.setRating(rating);
        testimonial.setServiceType(serviceType);
        testimonial.setFeedback(feedback);
        testimonial.setHighlight("Feedback từ khách hàng sau khi sử dụng dịch vụ.");
        testimonial.setAvatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1200&auto=format&fit=crop");
        testimonial.setPublished(true);
        return testimonial;
    }
}

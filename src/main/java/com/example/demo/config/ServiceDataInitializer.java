package com.example.demo.config;

import com.example.demo.model.WeddingService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceDataInitializer implements CommandLineRunner {

    private final ServiceRepository serviceRepository;

    public ServiceDataInitializer(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(String... args) {
        if (serviceRepository.count() > 0) {
            return;
        }

        serviceRepository.saveAll(List.of(
                createService(
                        "Gói Ngày cưới",
                        "Trọn gói cho lễ gia tiên và tiệc cưới với concept sang trọng, tinh tế.",
                        "12.500.000 VNĐ",
                        "camera",
                        "https://images.unsplash.com/photo-1549333341-c7974feb1e0e?q=80&w=1200&auto=format&fit=crop",
                        "Bao gồm tư vấn concept, makeup cô dâu, chụp hình trong ngày cưới, chọn góc máy và chỉnh sửa hậu kỳ cơ bản."
                ),
                createService(
                        "Gói chụp album Pre Wedding",
                        "Dành cho những cặp đôi muốn có bộ ảnh cưới lãng mạn, hiện đại và giàu cảm xúc.",
                        "18.500.000 VNĐ",
                        "sparkles",
                        "https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1200&auto=format&fit=crop",
                        "Bao gồm album ngoại cảnh, stylist hỗ trợ trang phục, makeup đi theo suốt buổi chụp và hậu kỳ ảnh chọn lọc."
                ),
                createService(
                        "Quay Phóng sự ngày cưới Indoor/Outdoor",
                        "Ghi lại trọn vẹn khoảnh khắc bằng phong cách cinematic, tự nhiên và cảm xúc.",
                        "10.000.000 VNĐ",
                        "video",
                        "https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1200&auto=format&fit=crop",
                        "Bao gồm quay highlight, dựng phim ngắn, ghi hình toàn bộ nghi thức và bàn giao file video chất lượng cao."
                )
        ));
    }

    private WeddingService createService(
            String title,
            String shortDescription,
            String priceRange,
            String iconName,
            String imageUrl,
            String detailedDescription
    ) {
        WeddingService service = new WeddingService();
        service.setTitle(title);
        service.setShortDescription(shortDescription);
        service.setPriceRange(priceRange);
        service.setIconName(iconName);
        service.setImageUrl(imageUrl);
        service.setDetailedDescription(detailedDescription);
        return service;
    }
}
